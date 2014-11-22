package u1171639.test.integration;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.net.ConnectException;

import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.controller.AuctionController;
import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.AuthorisationException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.model.account.User;
import u1171639.main.model.lot.Car;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.AccountService;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.HighestBid;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.SpaceUtils;
import u1171639.main.view.AuctionView;
import u1171639.test.mock.MockAccountService;

public class DummyView implements AuctionView {

	private AuctionController controller;
	private Object object;
	private JavaSpace space;
	
	private static final String USER_EMAIL = "user@testing.com";
	private static final String USER_PASSWORD = "password";
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace("localhost");
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		LotService lotService = new JavaSpaceLotService(space);
		AccountService accountService = new MockAccountService();
		
		new AuctionController(this, lotService, accountService);
		
		LotIDCounter.initialiseInSpace(space);
		
		// Generate a user that we can use during testing
		User user = new User();
		user.email = USER_EMAIL;
		user.password = USER_PASSWORD;
		this.controller.register(user);
	}
	
	

	@After
	public void tearDown() throws Exception {
		Lot template = new Lot();
		
		for(;;) {
			if(this.space.takeIfExists(template, null, 0) == null) {
				break;
			}
		}
	}
	
	@Test
	public void registerTest() {
		User user = new User();
		user.email = "registration@testing.com";
		user.password = "password";
		
		try {
			this.controller.register(user);
		} catch(RegistrationException e) {
			fail("Registering new user failed.");
		}
		
		try {
			this.controller.register(user);
			fail("User with non-unique email added.");
		} catch(RegistrationException e) {
			// Passed
		}
	}

	@Test
	public void testAddLot() throws Exception {
		Car car = new Car();
		car.make = "Ford";
		car.model = "Focus";
		
		try {
			this.controller.addLot(car);
			fail("Lot added without being logged in");
		} catch(AuthorisationException e) {
			// Pass
		}
		
		this.logIn();
		
		try {
			long carId = this.controller.addLot(car);
			assertTrue(carId >= 0);
			
			long carId2 = this.controller.addLot(car);
			assertTrue(carId2 == carId + 1);
			
			HighestBid template1 = new HighestBid(carId);
			HighestBid template2 = new HighestBid(carId2);
			
			HighestBid carBid1 = (HighestBid) space.readIfExists(template1, null, 0);
			HighestBid carBid2 = (HighestBid) space.readIfExists(template2, null, 0);
			
			assertNotNull(carBid1);
			assertNotNull(carBid2);
			assertEquals(carBid1.bidId, HighestBid.NO_BID_ID);
			assertEquals(carBid2.bidId, HighestBid.NO_BID_ID);
		} catch(AuthorisationException e) {
			fail("AuthorisationException despite being logged in.");
		} finally {
			this.logout();
		}
	}
	
	
	@Test
	public void testBidForLot() throws AuthorisationException {
		Car car = new Car();
		car.make = "Test";
		car.model = "BidForLot";
		
		Car car2 = new Car();
		car2.make = "Test2";
		car2.model = "BidForLot2";
		
		Car car3 = new Car();
		car3.make = "Test3";
		car3.model = "BidForLot3";
		
		try {
			this.controller.bidForLot(-1, new BigDecimal(100.00));
			fail("Bid made without being logged in");
		} catch(AuthorisationException e) {
			// Pass
		}
		
		this.logIn();
		
		try {
			car.id = this.controller.addLot(car);
			car2.id = this.controller.addLot(car2);
			car3.id = this.controller.addLot(car3);

			assertTrue(car2.id == car.id + 1);
			assertTrue(car3.id == car2.id + 1);
			
			// TODO Adding a bid
		} catch(AuthorisationException e) {
			fail("AuthorisationException despite being logged in.");
		} finally {
			this.logout();
		}
		
	}
	
	public void testGetHighestBid() {
		
	}
	
	public void  testUpdateLot() {
		
	}
	
	
	@Test
	public void testSubscribeToLot() throws Exception {
		Car car = new Car();
		car.make = "Mazda";
		car.model = "RX8";
		
		final Object finished = new Object();
		
		Callback<Void, Lot> callback = new Callback<Void, Lot>() {
			@Override
			public Void call(Lot lot) {
				object = lot;
				
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		};
		
		try {
			this.controller.subscribeToLot(0, callback);
			fail("Lot subscribed to without being logged in");
		} catch(AuthorisationException e) {
			// Pass
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
		}
		
		
		this.logIn();
		
		try {		
			car.id = this.controller.addLot(car);
			this.controller.subscribeToLot(car.id, callback);
			this.controller.updateLot(car);
			
			synchronized(finished) {
				finished.wait();
			}
			
			Car retrievedCar = (Car) this.object;
			assertNotSame(car, retrievedCar);
			assertEquals(car.make, retrievedCar.make);
			assertEquals(car.model, retrievedCar.model);
		} catch(AuthorisationException e) {
			fail("AuthorisationException despite being logged in.");
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
			
			this.logout();
			this.space.take(car, null, Lease.FOREVER);
		}
	}
	
	
	
	@Test
	public void testListenForLot() throws Exception {
		Car car = new Car();
		car.make = "Honda";
		car.model = "Civic";
		
		final Object finished = new Object();
		
		Callback<Void, Lot> callback = new Callback<Void, Lot>() {
			@Override
			public Void call(Lot lot) {
				object = lot;
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		};
				
		try {
			this.controller.listenForLot(car, callback);
			fail("Listened for lot without being logged in");
		} catch(AuthorisationException e) {
			// Pass
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
		}
		
		this.logIn();
		
		try {
			synchronized(finished) {
				this.controller.listenForLot(car, callback);
				this.controller.addLot(car);
				finished.wait();
			}
			
			Car retrievedCar = (Car) this.object;
			assertNotSame(car, retrievedCar);
			assertEquals(car.make, retrievedCar.make);
			assertEquals(car.model, retrievedCar.model);
			this.space.take(car, null, Lease.FOREVER);
		} catch(AuthorisationException e) {
			fail("AuthorisationException despite being logged in.");
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
			
			this.logout();
		}
	}
	
	

	@Override
	public void init(AuctionController controller) {
		this.controller = controller;
		
	}
	
	private User logIn() {
		User credentials = new User();
		credentials.email = USER_EMAIL;
		credentials.password = USER_PASSWORD;

		try {
			User user = this.controller.login(credentials);
			return user;
		} catch (AuthenticationException e) {
			fail("Failed to log in with default credentials.");
			return null;
		}
		
	}
	
	private void logout() {
		this.controller.logout();
	}
}
