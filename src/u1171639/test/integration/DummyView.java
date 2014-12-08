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
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.service.AccountService;
import u1171639.main.java.service.JavaSpaceAccountService;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.service.JavaSpaceNotificationService;
import u1171639.main.java.service.LotService;
import u1171639.main.java.service.NotificationService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.MediumSecurityHashScheme;
import u1171639.main.java.utilities.PasswordHashScheme;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.counters.BidIDCounter;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.main.java.utilities.counters.NotificationIDCounter;
import u1171639.main.java.utilities.counters.UserIDCounter;
import u1171639.main.java.view.AuctionView;
import u1171639.test.utilities.TestUtils;

public class DummyView implements AuctionView {

	private AuctionController controller;
	private Object object;
	private JavaSpace space;
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace(SpaceConsts.HOST);
		if(this.space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TransactionManager transMgr = SpaceUtils.getManager(SpaceConsts.HOST);
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
		BidIDCounter.initialiseInSpace(this.space);
		NotificationIDCounter.initialiseInSpace(this.space);
	}
	
	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new User(), this.space);
		TestUtils.removeAllFromSpace(new Bid(), this.space);
		TestUtils.removeAllFromSpace(new Lot(), this.space);
		TestUtils.removeAllFromSpace(new Notification(), this.space);
	}
	
	// Account Tests
	
	@Test
	public void testRegister() {
		User user = new User();
		user.username = "registration@testing.com";
		user.password = "password";
		
		try {
			this.controller.register(user);
		} catch(RegistrationException e) {
			fail("Registering new user failed.");
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.controller.register(user);
			fail("User with non-unique username added.");
		} catch(RegistrationException e) {
			// Passed
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Test
	public void testLogin() {
		User newUser = new User();
		newUser.username = "test@login.com";
		newUser.password = "password";
		
		try {
			newUser.id = this.controller.register(newUser);
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Test that incorrect username stops login
		try {
			User credentials = new User();
			credentials.username = "test@incorrect.com";
			credentials.username = "password";
			this.controller.login(credentials);
			fail("Incorrect username. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Test that a correct username but incorrect password stops login
		try {
			User credentials = new User();
			credentials.username = "test@login.com";
			credentials.username = "incorrectPass";
			this.controller.login(credentials);
			fail("Incorrect password. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// Test that a correct username and password allows login
		try {
			User credentials = new User();
			credentials.username = "test@login.com";
			credentials.password = "password";
			this.controller.login(credentials);
		} catch(AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.controller.logout();
		}
	}
	
	@Test
	public void testLogout() {
		User newUser = new User();
		newUser.username = "test@logout.com";
		newUser.password = "password";
		
		// Make sure no one is logged in to start with
		assertFalse(this.controller.isLoggedIn());
		
		register(newUser);
		login(newUser);
		
		User retrievedCurrentUser = this.controller.getCurrentUser();
		
		// Test we can get the current user and that we are logged in
		assertNotNull(retrievedCurrentUser);
		assertTrue(newUser.id.equals(retrievedCurrentUser.id));
		assertTrue(newUser.username.equals(retrievedCurrentUser.username));
		assertTrue(this.controller.isLoggedIn());
		
		this.controller.logout();
		
		// Test that logout works
		assertFalse(this.controller.isLoggedIn());
	}
	
	@Test
	public void testGetCurrentUser() {
		User user1 = new User();
		user1.username = "test@currentUser1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.username = "test@currentUser2.com";
		user2.password = "password2";
		
		register(user1);
		register(user2);
		
		try {
			// Test logging in with user1 sets user1 as the current user
			this.controller.login(user1);
			User retrievedCurrentUser = this.controller.getCurrentUser();
						
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user1.id));
			assertTrue(retrievedCurrentUser.username.equals(user1.username));
			assertTrue(this.controller.isLoggedIn());
			
			this.controller.logout();
			
			// Test logging in with user2 sets user  as the current user
			this.controller.login(user2);
			retrievedCurrentUser = this.controller.getCurrentUser();
			
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user2.id));
			assertTrue(retrievedCurrentUser.username.equals(user2.username));
			assertTrue(this.controller.isLoggedIn());
		} catch (AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			this.controller.logout();
		}
	}
	
	@Test
	public void testGetUserDetails() {
		User user1 = new User();
		user1.username = "test@getUserDetails1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.username = "test@getUserDetails2.com";
		user2.password = "password2";
		
		register(user1);
		register(user2);
		
		// Test we can retrieve registered users using their ID
		try {
			User retrievedUserId1 = this.controller.getUserDetails(user1.id);
			User retrievedUserId2 = this.controller.getUserDetails(user2.id);
			
			assertEquals(user1.id, retrievedUserId1.id);
			assertEquals(user1.username, retrievedUserId1.username);
			
			assertEquals(user2.id, retrievedUserId2.id);
			assertEquals(user2.username, retrievedUserId2.username);
		} catch (UserNotFoundException e) {
			fail("Users were registered so should be able to retrieve details.");
		}
		
		// Test that we can get registered users using their username
		try {
			User retrievedUserId1 = this.controller.getUserDetails(user1.username);
			User retrievedUserId2 = this.controller.getUserDetails(user2.username);
			
			assertEquals(user1.id, retrievedUserId1.id);
			assertEquals(user1.username, retrievedUserId1.username);
			
			assertEquals(user2.id, retrievedUserId2.id);
			assertEquals(user2.username, retrievedUserId2.username);
		} catch (UserNotFoundException e) {
			fail("Users were registered so should be able to retrieve details.");
		}
		
		// Test that trying to retrieve unregistered users results in an exception
		try {
			this.controller.getUserDetails("madeup@username.com");
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
		user1.username = "test@removeUser1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.username = "test@removeUser2.com";
		user2.password = "password2";
		
		// Test we can remove user using their ID
		try {
			user1.id = this.controller.register(user1);
			this.controller.getUserDetails(user1.id);
		} catch (RegistrationException e) {
			fail("Unique users - should have been registered.");
		} catch (UserNotFoundException e) {
			fail("User was registered so should be able to be found.");
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		
		// Test that we can removed a user using their username
		try {
			user2.id = this.controller.register(user2);
			this.controller.getUserDetails(user2.username);
		} catch (RegistrationException e) {
			fail("Unique users - should have been registered.");
		} catch (UserNotFoundException e) {
			fail("User was registered so should be able to be found.");
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.controller.removeUser(user2.username);
		} catch(UserNotFoundException e) {
			fail("User was registered so should be able to be removed.");
		}
		
		try {
			this.controller.getUserDetails(user2.username);
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
		user.username = "test@addLot.com";
		user.password = "password";
		
		register(user);
		
		try {
			this.controller.addLot(car, null);
			fail("Lot added without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		login(user);
		
		try {
			long carId = this.controller.addLot(car, null);
			long carId2 = this.controller.addLot(car, null);
			assertEquals(carId2, carId + 1);
			
			Bid carBid1 = this.controller.getHighestBid(carId);
			Bid carBid2 = this.controller.getHighestBid(carId2);
			
			
			assertNull(carBid1);
			assertNull(carBid2);
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
		user.username = "test@searchLots.com";
		user.password = "password";
		
		register(user);
		login(user);

		
		try {
			this.controller.addLot(car1, null);
			this.controller.addLot(car2, null);
			this.controller.addLot(car3, null);
			this.controller.addLot(car4, null);
			
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
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (UserNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		user.username = "test@updateLot.com";
		user.password = "password";
		
		register(user);
		login(user);
		
		try {
			car.id = this.controller.addLot(car, null);
			
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
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
		user1.username = "test@bidForLot1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.username = "test@bidForLot2.com";
		user2.password = "password2";
		
		register(user1);
		register(user2);
		
		try {
			this.controller.bidForLot(new Bid(-1, new BigDecimal(100.00), false));
			fail("Bid made without being logged in");
		} catch(UnauthorisedBidException | InvalidBidException e) {
			fail("Bid attempted without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		} catch (LotNotFoundException e) {
			fail("Lot exist. Should have been found.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			this.controller.login(user1);
		} catch (AuthenticationException e) {
			fail("User was registered. Should be able to login");
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			car.id = this.controller.addLot(car, null);
			car2.id = this.controller.addLot(car2, null);
			car3.id = this.controller.addLot(car3, null);

			assertTrue(car2.id.equals(car.id + 1));
			assertTrue(car3.id.equals(car2.id + 1));
			
			try {
				this.controller.bidForLot(new Bid(car.id, new BigDecimal(1000.00), false));
				fail("Should not have been able to bid for own Lot");
			} catch(UnauthorisedBidException e) {
				// Pass
			} catch (LotNotFoundException e) {
				fail("Lot exists. Should have been found.");
			} finally {
				this.controller.logout();
			}
			
			// Log in with a different user
			login(user2);
			
			this.controller.bidForLot(new Bid(car.id, new BigDecimal(1000.00), false));
			Bid highestBid1 = this.controller.getHighestBid(car.id);
			
			assertTrue(highestBid1.bidderId.equals(user2.id));
			assertEquals(highestBid1.lotId, car.id);
			assertTrue(highestBid1.amount.compareTo(new BigDecimal(1000.00)) == 0);
			assertTrue(highestBid1.privateBid == false);
			
			try {
				this.controller.bidForLot(new Bid(car.id, new BigDecimal(100.00), false));
				highestBid1 = this.controller.getHighestBid(car.id);
				fail("Bid should exceed current bid amount");
			} catch(InvalidBidException e) {
				// Pass
			}
			
			assertEquals(highestBid1.lotId, car.id);
			assertTrue(highestBid1.amount.compareTo(new BigDecimal(1000.00)) == 0);
			assertTrue(highestBid1.privateBid == false);
			
			this.controller.bidForLot(new Bid(car.id, new BigDecimal(1001.00), false));
			highestBid1 = this.controller.getHighestBid(car.id);
			
			assertEquals(highestBid1.lotId, car.id);
			assertTrue(highestBid1.amount.compareTo(new BigDecimal(1001.00)) == 0);
			assertTrue(highestBid1.privateBid == false);
			
			assertNull(this.controller.getHighestBid(car2.id));
			
			try {
				this.controller.bidForLot(new Bid(car2.id, new BigDecimal(-500.00), false));
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
		} catch (LotNotFoundException e) {
			fail("Lot exists. Should have been found.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (ValidationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
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
		user1.username = "test@getVisibleLots1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.username = "test@getVisibleLots2.com";
		user2.password = "password2";
		
		User user3 = new User();
		user3.username = "test@getVisibleLots3.com";
		user3.password = "password3";
		
		register(user1);
		register(user2);
		register(user3);
		
		login(user1);
		
		try {
			car.id = this.controller.addLot(car, null);
		} catch (RequiresLoginException e1) {
			fail("User logged in. Lot should have been added.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.controller.logout();
		login(user2);	
		
		try {
			this.controller.bidForLot(new Bid(car.id, new BigDecimal(100.00), false));
			this.controller.bidForLot(new Bid(car.id, new BigDecimal(200.00), true));
			
			this.controller.logout();
			login(user3);	
			
			this.controller.bidForLot(new Bid(car.id, new BigDecimal(300.00), true));
			this.controller.bidForLot(new Bid(car.id, new BigDecimal(400.00), false));
			this.controller.bidForLot(new Bid(car.id, new BigDecimal(500.00), false));
		} catch (UnauthorisedBidException | InvalidBidException e) {
			fail("Bid was valid. Exception should not have been thrown.");
		} catch(RequiresLoginException e) {
			fail("User was logged in");
		} catch (LotNotFoundException e1) {
			fail("Lots existed. Should have been found.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		this.controller.logout();
		login(user2);
		
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
			login(user3);	
		
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
			login(user1);
			
			List<Bid> visibleBids3 = this.controller.getVisibleBids(car.id);
			assertTrue(visibleBids3.size() == 5);
			
		} catch (RequiresLoginException e) {
			fail("User was logged in.");
		} catch (LotNotFoundException e) {
			fail("Lots exist. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testSubscribeToLot() throws Exception {
		Car car = new Car();
		car.make = "Mazda";
		car.model = "RX8";
		
		User user = new User();
		user.username = "test@subscribeToLot.com";
		user.password = "password";
		
		register(user);
		
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
			this.controller.listenForLotUpdates(0, callback);
			fail("Lot subscribed to without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
		}
		
		login(user);
		
		try {		
			car.id = this.controller.addLot(car, null);
			this.controller.listenForLotUpdates(car.id, callback);
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
		user.username = "test@subscribeToLot.com";
		user.password = "password";
		
		register(user);
		
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
			this.controller.listenForLotAddition(car, callback);
			fail("Listened for lot without being logged in");
		} catch(RequiresLoginException e) {
			// Pass
		} finally {
			synchronized(finished) {
				finished.notifyAll();
			}
		}
		
		login(user);	
		
		try {
			synchronized(finished) {
				this.controller.listenForLotAddition(car, callback);
				this.controller.addLot(car, null);
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
	
	@Test
	public void retrieveAllNotifications() {
		Notification notification1 = new Notification();
		notification1.title = "Testing Title 1";
		notification1.message = "Testing Message 1";
		
		Notification notification2 = new Notification();
		notification2.title = "Testing Title 2";
		notification2.message = "Testing Message 2";
		
		Notification notification3 = new Notification();
		notification3.title = "Testing Title 3";
		notification3.message = "Testing Message 3";
		
		User user1 = new User();
		user1.username = "test@retrieveAllNotifications1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.username = "test@retrieveAllNotifications2.com";
		user2.password = "password2";
		
		register(user1);
		login(user1);
		
		try {
			this.controller.addNotification(notification1);
			this.controller.addNotification(notification2);
			
			List<Notification> retrievedNotifications1 = this.controller.retrieveAllNotifications();
			
			assertTrue(retrievedNotifications1.size() == 2);
			
			Collections.sort(retrievedNotifications1, new Comparator<Notification>() {
				@Override
				public int compare(Notification o1, Notification o2) {
					return Long.compare(o1.id, o2.id);
				}
			});
			
			assertEquals(retrievedNotifications1.get(0).title, "Testing Title 1");
			assertEquals(retrievedNotifications1.get(1).title, "Testing Title 2");
			
			
			register(user2);
			login(user2);
			
			this.controller.addNotification(notification3);
			
			List<Notification> retrievedNotifications2 = this.controller.retrieveAllNotifications();
			
			assertTrue(retrievedNotifications2.size() == 1);
					
			assertEquals(retrievedNotifications2.get(0).title, "Testing Title 3");
			
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (RequiresLoginException e) {
			fail("User logged in. Should have worked.");
		}
	}
	
	@Test
	public void testListenForNotifications() {
		final Notification notification1 = new Notification();
		notification1.title = "Testing Title 1";
		notification1.message = "Testing Message 1";
		
		User user = new User();
		user.username = "test@retrieveAllNotifications.com";
		user.password = "password";
		
		register(user);
		login(user);
		
		final Object lock = new Object();
		
		try {
			this.controller.listenForNotifications(new Callback<Notification, Void>() {

				@Override
				public Void call(Notification notification) {
					assertEquals(notification.title, notification1.title);
					assertEquals(notification.message, notification1.message);
					
					synchronized (lock) {
						lock.notify();
					}
					
					return null;
				}
			});
			
			this.controller.addNotification(notification1);
			synchronized (lock) {
				lock.wait();
			}
			
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (RequiresLoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			fail("Interruption Exception");
		}
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
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void login(User credentials) {
		try {
			this.controller.login(credentials);
		} catch (AuthenticationException e) {
			e.printStackTrace();
		} catch (ValidationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
