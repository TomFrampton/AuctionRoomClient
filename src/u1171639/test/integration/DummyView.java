package u1171639.test.integration;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.jini.core.lease.Lease;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.java.controller.AuctionController;
import u1171639.main.java.controller.ConcreteAuctionController;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.service.AccountService;
import u1171639.main.java.service.JavaSpaceAccountService;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.service.JavaSpaceNotificationService;
import u1171639.main.java.service.LotService;
import u1171639.main.java.service.NotificationService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.HighestBid;
import u1171639.main.java.utilities.LotIDCounter;
import u1171639.main.java.utilities.MediumSecurityHashScheme;
import u1171639.main.java.utilities.PasswordHashScheme;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.UserIDCounter;
import u1171639.main.java.view.AuctionView;
import u1171639.test.utilities.TestUtils;

public class DummyView implements AuctionView {

	private AuctionController controller;
	private Object object;
	private JavaSpace space;
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace("localhost");
		if(this.space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TransactionManager transMgr = SpaceUtils.getManager("localhost");
		if(transMgr == null) {
			throw new ConnectException("Could not connect to TransactionManager");
		}
		
		PasswordHashScheme hashScheme = new MediumSecurityHashScheme();
		
		LotService lotService = new JavaSpaceLotService(this.space, transMgr);
		NotificationService notificationService = new JavaSpaceNotificationService(this.space, transMgr);
		AccountService accountService = new JavaSpaceAccountService(this.space, hashScheme);
		
		AuctionController controller = new ConcreteAuctionController(this, lotService, accountService, notificationService);
		controller.launch();
		
		LotIDCounter.initialiseInSpace(this.space);
		UserIDCounter.initialiseInSpace(this.space);
	}
	
	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new User(), this.space);
		TestUtils.removeAllFromSpace(new Bid(), this.space);
		TestUtils.removeAllFromSpace(new Lot(), this.space);
	}
	
	// Account Tests
	
	@Test
	public void testRegister() {
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
	public void testLogin() {
		User newUser = new User();
		newUser.email = "test@login.com";
		newUser.password = "password";
		
		try {
			newUser.id = this.controller.register(newUser);
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		}
		
		// Test that incorrect email stops login
		try {
			User credentials = new User();
			credentials.email = "test@incorrect.com";
			credentials.email = "password";
			this.controller.login(credentials);
			fail("Incorrect email. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		}
		
		// Test that a correct email but incorrect password stops login
		try {
			User credentials = new User();
			credentials.email = "test@login.com";
			credentials.email = "incorrectPass";
			this.controller.login(credentials);
			fail("Incorrect password. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		}
		
		// Test that a correct email and password allows login
		try {
			User credentials = new User();
			credentials.email = "test@login.com";
			credentials.password = "password";
			this.controller.login(credentials);
		} catch(AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} finally {
			this.controller.logout();
		}
	}
	
	@Test
	public void testLogout() {
		User newUser = new User();
		newUser.email = "test@logout.com";
		newUser.password = "password";
		
		// Make sure no one is logged in to start with
		assertFalse(this.controller.isLoggedIn());
		
		this.register(newUser);
		this.login(newUser);
		
		User retrievedCurrentUser = this.controller.getCurrentUser();
		
		// Test we can get the current user and that we are logged in
		assertNotNull(retrievedCurrentUser);
		assertTrue(newUser.id.equals(retrievedCurrentUser.id));
		assertTrue(newUser.email.equals(retrievedCurrentUser.email));
		assertTrue(this.controller.isLoggedIn());
		
		this.controller.logout();
		
		// Test that logout works
		assertFalse(this.controller.isLoggedIn());
	}
	
	@Test
	public void testGetCurrentUser() {
		User user1 = new User();
		user1.email = "test@currentUser1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.email = "test@currentUser2.com";
		user2.password = "password2";
		
		this.register(user1);
		this.register(user2);
		
		try {
			// Test logging in with user1 sets user1 as the current user
			this.controller.login(user1);
			User retrievedCurrentUser = this.controller.getCurrentUser();
						
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user1.id));
			assertTrue(retrievedCurrentUser.email.equals(user1.email));
			assertTrue(this.controller.isLoggedIn());
			
			this.controller.logout();
			
			// Test logging in with user2 sets user  as the current user
			this.controller.login(user2);
			retrievedCurrentUser = this.controller.getCurrentUser();
			
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user2.id));
			assertTrue(retrievedCurrentUser.email.equals(user2.email));
			assertTrue(this.controller.isLoggedIn());
		} catch (AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} finally {
			this.controller.logout();
		}
	}
	
	@Test
	public void testGetUserDetails() {
		User user1 = new User();
		user1.email = "test@getUserDetails1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.email = "test@getUserDetails2.com";
		user2.password = "password2";
		
		this.register(user1);
		this.register(user2);
		
		// Test we can retrieve registered users using their ID
		try {
			User retrievedUserId1 = this.controller.getUserDetails(user1.id);
			User retrievedUserId2 = this.controller.getUserDetails(user2.id);
			
			assertEquals(user1.id, retrievedUserId1.id);
			assertEquals(user1.email, retrievedUserId1.email);
			
			assertEquals(user2.id, retrievedUserId2.id);
			assertEquals(user2.email, retrievedUserId2.email);
		} catch (UserNotFoundException e) {
			fail("Users were registered so should be able to retrieve details.");
		}
		
		// Test that we can get registered users using their email
		try {
			User retrievedUserId1 = this.controller.getUserDetails(user1.email);
			User retrievedUserId2 = this.controller.getUserDetails(user2.email);
			
			assertEquals(user1.id, retrievedUserId1.id);
			assertEquals(user1.email, retrievedUserId1.email);
			
			assertEquals(user2.id, retrievedUserId2.id);
			assertEquals(user2.email, retrievedUserId2.email);
		} catch (UserNotFoundException e) {
			fail("Users were registered so should be able to retrieve details.");
		}
		
		// Test that trying to retrieve unregistered users results in an exception
		try {
			this.controller.getUserDetails("madeup@email.com");
			fail("User doesn't exist. UserNotFoundException should have been thrown.");
		} catch(UserNotFoundException e) {
			// Pass
		}
		
		try {
			this.controller.getUserDetails(-1);
			fail("User doesn't exist. UserNotFoundException should have been thrown.");
		} catch(UserNotFoundException e) {
			// Pass
		}
	}
	
	@Test
	public void removeUser() {
		User user1 = new User();
		user1.email = "test@removeUser1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.email = "test@removeUser2.com";
		user2.password = "password2";
		
		// Test we can remove user using their ID
		try {
			user1.id = this.controller.register(user1);
			this.controller.getUserDetails(user1.id);
		} catch (RegistrationException e) {
			fail("Unique users - should have been registered.");
		} catch (UserNotFoundException e) {
			fail("User was registered so should be able to be found.");
		}
		
		try {
			this.controller.removeUser(user1.id);
		} catch(UserNotFoundException e) {
			fail("User was registered so should be able to be removed.");
		}
		
		try {
			this.controller.getUserDetails(user1.id);
			fail("User was removed so details should not be found");
		} catch(UserNotFoundException e) {
			// Pass
		}
		
		// Test that we can removed a user using their email
		try {
			user2.id = this.controller.register(user2);
			this.controller.getUserDetails(user2.email);
		} catch (RegistrationException e) {
			fail("Unique users - should have been registered.");
		} catch (UserNotFoundException e) {
			fail("User was registered so should be able to be found.");
		}
		
		try {
			this.controller.removeUser(user2.email);
		} catch(UserNotFoundException e) {
			fail("User was registered so should be able to be removed.");
		}
		
		try {
			this.controller.getUserDetails(user2.email);
			fail("User was removed so details should not be found");
		} catch(UserNotFoundException e) {
			// Pass
		}
	}
	
	// Lot Tests
	
	@Test
	public void testAddLot() {
		Car car = new Car();
		car.make = "Ford";
		car.model = "Focus";
		
		User user = new User();
		user.email = "test@addLot.com";
		user.password = "password";
		
		this.register(user);
		
		try {
			this.controller.addLot(car);
			fail("Lot added without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		}
		
		this.login(user);
		
		try {
			long carId = this.controller.addLot(car);
			long carId2 = this.controller.addLot(car);
			assertEquals(carId2, carId + 1);
			
			HighestBid template1 = new HighestBid(carId);
			HighestBid template2 = new HighestBid(carId2);
			
			HighestBid carBid1 = (HighestBid) this.space.readIfExists(template1, null, 0);
			HighestBid carBid2 = (HighestBid) this.space.readIfExists(template2, null, 0);
			
			assertNotNull(carBid1);
			assertNotNull(carBid2);
			assertEquals(carBid1.bidId, HighestBid.NO_BID_ID);
			assertEquals(carBid2.bidId, HighestBid.NO_BID_ID);
		} catch(RequiresLoginException e) {
			fail("AuthorisationException despite being logged in.");
		} catch(Exception e) {
			fail(e.toString());
		} finally {
			this.controller.logout();
		}
	}
	
	@Test
	public void testSearchForLot() {
		Car car1 = new Car();
		car1.make = "Honda"; 
		car1.model = "Civic";
		car1.description = "A really nice car!";
		
		Car car2 = new Car();
		car2.make = "Honda"; 
		car2.model = "NSX";
		car2.description = "A really nice car!";
		
		Car car3 = new Car();
		car3.make = "Ford"; 
		car3.model = "Focus";
		car3.description = "A really rubbish car!";
		
		Car car4 = new Car();
		car4.make = "Ford"; 
		car4.model = "Fiesta";
		car4.description = "A really nice car!";
		
		User user = new User();
		user.email = "test@searchLots.com";
		user.password = "password";
		
		this.register(user);
		this.login(user);

		
		try {
			this.controller.addLot(car1);
			this.controller.addLot(car2);
			this.controller.addLot(car3);
			this.controller.addLot(car4);
			
			// Search 1
			Car template1 = new Car();
			template1.make = "Honda";
			
			List<Lot> search1 = this.controller.searchLots(template1);
			
			assertTrue(search1.size() == 2);
			
			for(Lot lot : search1) {
				Car car = (Car) lot;
				assertEquals(car.make, template1.make);
			}
			
			// Search 2
			Car template2 = new Car();
			template2.make = "Ford";
			
			List<Lot> search2 = this.controller.searchLots(template2);
			
			assertTrue(search2.size() == 2);
			
			for(Lot lot : search2) {
				Car car = (Car) lot;
				assertEquals(car.make, template2.make);
			}
			
			// Search 3
			Car template3 = new Car();
			template3.sellerId = user.id;
			
			List<Lot> search3 = this.controller.searchLots(template3);
			
			assertTrue(search3.size() == 4);
			
			int fords = 0;
			int hondas = 0;
			
			for(Lot lot : search3) {
				Car car = (Car) lot;
				assertTrue(car.sellerId.equals(user.id));
				
				if(car.make.equals("Honda")) {
					hondas++;
				} else if(car.make.equals("Ford")) {
					fords++;
				}
			}
			
			assertTrue(fords == 2);
			assertTrue(hondas == 2);
			
			// Search 4
			Car template4 = new Car();
			template4.description = "A really nice car!";
			
			List<Lot> search4 = this.controller.searchLots(template4);
			
			assertTrue(search4.size() == 3);
			
			// Search 5
			Car template5 = new Car();
			List<Lot> search5 = this.controller.searchLots(template5);
			
			assertTrue(search5.size() == 4);
		} catch(RequiresLoginException e) {
			fail("User should have logged in.");
		} finally {
			this.controller.logout();
		}
	}
	
	@Test
	public void testGetUsersLots() {
		// TODO
	}
	
	@Test
	public void  testUpdateLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "UpdateLot";
		car.sellerId = 0l;
		
		User user = new User();
		user.email = "test@updateLot.com";
		user.password = "password";
		
		this.register(user);
		this.login(user);
		
		try {
			car.id = this.controller.addLot(car);
			
			car.description = "Updated!";
			this.controller.updateLot(car);
			
			Car retrievedCar = (Car) this.controller.getLotDetails(car.id);
			
			assertTrue(retrievedCar.id.equals(car.id));
			assertTrue(retrievedCar.make.equals(car.make));
			assertTrue(retrievedCar.model.equals(car.model));
			assertTrue(retrievedCar.sellerId.equals(car.sellerId));
			assertTrue(retrievedCar.description.equals(car.description));
		} catch(RequiresLoginException e) {
			fail("User should have logged in.");
		} catch (LotNotFoundException e) {
			fail("Lot was added. Should have been found.");
			// TODO test LotNotFound
		} finally {
			this.controller.logout();
		}
	}	
	
	@Test
	public void testBidForLot() {
		Car car = new Car();
		car.make = "Test";
		car.model = "BidForLot";
		
		Car car2 = new Car();
		car2.make = "Test2";
		car2.model = "BidForLot2";
		
		Car car3 = new Car();
		car3.make = "Test3";
		car3.model = "BidForLot3";
		
		User user1 = new User();
		user1.email = "test@bidForLot1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.email = "test@bidForLot2.com";
		user2.password = "password2";
		
		this.register(user1);
		this.register(user2);
		
		try {
			this.controller.bidForLot(-1, new BigDecimal(100.00), false);
			fail("Bid made without being logged in");
		} catch(UnauthorisedBidException | InvalidBidException e) {
			fail("Bid attempted without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		} catch (LotNotFoundException e) {
			fail("Lot exist. Should have been found.");
		}
		
		try {
			this.controller.login(user1);
		} catch (AuthenticationException e) {
			fail("User was registered. Should be able to login");
		}
		
		try {
			car.id = this.controller.addLot(car);
			car2.id = this.controller.addLot(car2);
			car3.id = this.controller.addLot(car3);

			assertTrue(car2.id.equals(car.id + 1));
			assertTrue(car3.id.equals(car2.id + 1));
			
			try {
				this.controller.bidForLot(car.id, new BigDecimal(1000.00), false);
				fail("Should not have been able to bid for own Lot");
			} catch(UnauthorisedBidException e) {
				// Pass
			} catch (LotNotFoundException e) {
				fail("Lot exists. Should have been found.");
			} finally {
				this.controller.logout();
			}
			
			// Log in with a different user
			this.login(user2);
			
			this.controller.bidForLot(car.id, new BigDecimal(1000.00), false);
			Bid highestBid1 = this.controller.getHighestBid(car.id);
			
			assertTrue(highestBid1.id.equals(0l));
			assertEquals(highestBid1.lotId, car.id);
			assertTrue(highestBid1.amount.compareTo(new BigDecimal(1000.00)) == 0);
			assertTrue(highestBid1.privateBid == false);
			
			try {
				this.controller.bidForLot(car.id, new BigDecimal(100.00), false);
				highestBid1 = this.controller.getHighestBid(car.id);
				fail("Bid should exceed current bid amount");
			} catch(InvalidBidException e) {
				// Pass
			}
			
			assertTrue(highestBid1.id.equals(0l));
			assertEquals(highestBid1.lotId, car.id);
			assertTrue(highestBid1.amount.compareTo(new BigDecimal(1000.00)) == 0);
			assertTrue(highestBid1.privateBid == false);
			
			this.controller.bidForLot(car.id, new BigDecimal(1001.00), false);
			highestBid1 = this.controller.getHighestBid(car.id);
			
			assertTrue(highestBid1.id.equals(1l));
			assertEquals(highestBid1.lotId, car.id);
			assertTrue(highestBid1.amount.compareTo(new BigDecimal(1001.00)) == 0);
			assertTrue(highestBid1.privateBid == false);
			
			assertNull(this.controller.getHighestBid(car2.id));
			
			try {
				this.controller.bidForLot(car2.id, new BigDecimal(-500.00), false);
				fail("Bid amount should be greater than zero");
			} catch (InvalidBidException e) {
				// Pass
			}
			Bid highestBid2 = this.controller.getHighestBid(car2.id);
			assertNull(highestBid2);
			
		} catch(RequiresLoginException e) {
			fail("AuthorisationException despite being logged in.");
		} catch (UnauthorisedBidException e) {
			fail("Should have been able to bid on Lot.");
		} catch(InvalidBidException e) {
			fail("Bid was valid. Should have been accepted");
		} catch (LotNotFoundException e1) {
			fail("Lot exists. Should have been found.");
		} finally {
			this.controller.logout();
		}
	}
	
	@Test
	public void testGetVisibleBids() {
		Car car = new Car();
		car.make = "IntegrationTest"; 
		car.model = "GetVisibleLots";
		
		User user1 = new User();
		user1.email = "test@getVisibleLots1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.email = "test@getVisibleLots2.com";
		user2.password = "password2";
		
		User user3 = new User();
		user3.email = "test@getVisibleLots3.com";
		user3.password = "password3";
		
		this.register(user1);
		this.register(user2);
		this.register(user3);
		
		this.login(user1);
		
		try {
			car.id = this.controller.addLot(car);
		} catch (RequiresLoginException e1) {
			fail("User logged in. Lot should have been added.");
		}
		
		this.controller.logout();
		this.login(user2);	
		
		try {
			this.controller.bidForLot(car.id, new BigDecimal(100.00), false);
			this.controller.bidForLot(car.id, new BigDecimal(200.00), true);
			
			this.controller.logout();
			this.login(user3);	
			
			this.controller.bidForLot(car.id, new BigDecimal(300.00), true);
			this.controller.bidForLot(car.id, new BigDecimal(400.00), false);
			this.controller.bidForLot(car.id, new BigDecimal(500.00), false);
		} catch (UnauthorisedBidException | InvalidBidException e) {
			fail("Bid was valid. Exception should not have been thrown.");
		} catch(RequiresLoginException e) {
			fail("User was logged in");
		} catch (LotNotFoundException e1) {
			fail("Lots existed. Should have been found.");
		}
		
		this.controller.logout();
		this.login(user2);
		
		try {
			List<Bid> visibleBids1 = this.controller.getVisibleBids(car.id);
			Collections.sort(visibleBids1, new Comparator<Bid>() {
				@Override
				public int compare(Bid o1, Bid o2) {
					return Long.compare(o1.id, o2.id);
				}
			});
			
			assertTrue(visibleBids1.size() == 4);
			
			assertTrue(visibleBids1.get(0).lotId.equals(car.id));
			assertTrue(visibleBids1.get(0).amount.equals(new BigDecimal(100.00)));
			assertTrue(visibleBids1.get(0).bidderId.equals(user2.id));
			assertTrue(visibleBids1.get(0).privateBid == false);
			
			assertTrue(visibleBids1.get(1).lotId.equals(car.id));
			assertTrue(visibleBids1.get(1).amount.equals(new BigDecimal(200.00)));
			assertTrue(visibleBids1.get(1).bidderId.equals(user2.id));
			assertTrue(visibleBids1.get(1).privateBid == true);
			
			assertTrue(visibleBids1.get(2).lotId.equals(car.id));
			assertTrue(visibleBids1.get(2).amount.equals(new BigDecimal(400.00)));
			assertTrue(visibleBids1.get(2).bidderId.equals(user3.id));
			assertTrue(visibleBids1.get(2).privateBid == false);
			
			assertTrue(visibleBids1.get(3).lotId.equals(car.id));
			assertTrue(visibleBids1.get(3).amount.equals(new BigDecimal(500.00)));
			assertTrue(visibleBids1.get(3).bidderId.equals(user3.id));
			assertTrue(visibleBids1.get(3).privateBid == false);
		
			this.controller.logout();
			this.login(user3);	
		
			List<Bid> visibleBids2 = this.controller.getVisibleBids(car.id);
			
			Collections.sort(visibleBids2, new Comparator<Bid>() {
				@Override
				public int compare(Bid o1, Bid o2) {
					return Long.compare(o1.id, o2.id);
				}
			});
			
			assertTrue(visibleBids2.size() == 4);
			
			assertTrue(visibleBids2.get(0).lotId.equals(car.id));
			assertTrue(visibleBids2.get(0).amount.equals(new BigDecimal(100.00)));
			assertTrue(visibleBids2.get(0).bidderId.equals(user2.id));
			assertTrue(visibleBids2.get(0).privateBid == false);
			
			assertTrue(visibleBids2.get(1).lotId.equals(car.id));
			assertTrue(visibleBids2.get(1).amount.equals(new BigDecimal(300.00)));
			assertTrue(visibleBids2.get(1).bidderId.equals(user3.id));
			assertTrue(visibleBids2.get(1).privateBid == true);
			
			assertTrue(visibleBids2.get(2).lotId.equals(car.id));
			assertTrue(visibleBids2.get(2).amount.equals(new BigDecimal(400.00)));
			assertTrue(visibleBids2.get(2).bidderId.equals(user3.id));
			assertTrue(visibleBids2.get(2).privateBid == false);
			
			assertTrue(visibleBids2.get(3).lotId.equals(car.id));
			assertTrue(visibleBids2.get(3).amount.equals(new BigDecimal(500.00)));
			assertTrue(visibleBids2.get(3).bidderId.equals(user3.id));
			assertTrue(visibleBids2.get(3).privateBid == false);
			
			this.controller.logout();
			this.login(user1);
			
			List<Bid> visibleBids3 = this.controller.getVisibleBids(car.id);
			assertTrue(visibleBids3.size() == 5);
			
		} catch (RequiresLoginException e) {
			fail("User was logged in.");
		} catch (LotNotFoundException e) {
			fail("Lots exist. Should have been found");
		}
	}
	
	@Test
	public void testSubscribeToLot() throws Exception {
		Car car = new Car();
		car.make = "Mazda";
		car.model = "RX8";
		
		User user = new User();
		user.email = "test@subscribeToLot.com";
		user.password = "password";
		
		this.register(user);
		
		final Object finished = new Object();
		
		Callback<Lot, Void> callback = new Callback<Lot, Void>() {
			@Override
			public Void call(Lot lot) {
				DummyView.this.object = lot;
				
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		};
		
		try {
			this.controller.subscribeToLot(0, callback);
			fail("Lot subscribed to without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
		}
		
		this.login(user);
		
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
		} catch(RequiresLoginException e) {
			fail("AuthorisationException despite being logged in.");
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
			
			this.controller.logout();
			this.space.take(car, null, Lease.FOREVER);
		}
	}
	
	
	@Test
	public void testListenForLot() throws Exception {
		Car car = new Car();
		car.make = "Honda";
		car.model = "Civic";
		
		User user = new User();
		user.email = "test@subscribeToLot.com";
		user.password = "password";
		
		this.register(user);
		
		final Object finished = new Object();
		
		Callback<Lot, Void> callback = new Callback<Lot, Void>() {
			@Override
			public Void call(Lot lot) {
				DummyView.this.object = lot;
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		};
				
		try {
			this.controller.listenForLot(car, callback);
			fail("Listened for lot without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
		}
		
		this.login(user);	
		
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
		} catch(RequiresLoginException e) {
			fail("AuthorisationException despite being logged in.");
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
			
			this.controller.logout();
		}
	}
	
	@Test
	public void testRemoveLot() {
		//TODO
	}

	@Override
	public void start(AuctionController controller) {
		this.controller = controller;
	}
	
	private void register(User newUser) {
		try {
			newUser.id = this.controller.register(newUser);
		} catch (RegistrationException e) {
			e.printStackTrace();
		}
	}
	
	private void login(User credentials) {
		try {
			this.controller.login(credentials);
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
	}
}
