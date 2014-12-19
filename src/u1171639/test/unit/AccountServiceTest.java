package u1171639.test.unit;

import static org.junit.Assert.*;

import java.net.ConnectException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.UserAccount;
import u1171639.main.java.service.JavaSpaceAccountService;
import u1171639.main.java.utilities.MediumSecurityHashScheme;
import u1171639.main.java.utilities.PasswordHashScheme;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.counters.IDCounter;
import u1171639.main.java.utilities.counters.UserIDCounter;
import u1171639.test.utilities.TestUtils;

public class AccountServiceTest {
	private JavaSpaceAccountService accountService;
	private PasswordHashScheme hashScheme;
	
	private JavaSpace space;
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace(SpaceConsts.HOST);
		if(this.space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TransactionManager transMgr = SpaceUtils.getManager(SpaceConsts.HOST);
		if(transMgr == null) {
			throw new ConnectException("Could not connect to Transaction service");
		}
		
		this.hashScheme = new MediumSecurityHashScheme();
		this.accountService = new JavaSpaceAccountService(this.space, this.hashScheme, transMgr);
		
		IDCounter.initialiseInSpace(UserIDCounter.class, this.space);
	}

	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new UserAccount(), this.space);
	}
	
	@Test
	public void testRegister() {
		UserAccount newUser = new UserAccount();
		newUser.username = "test@register.com";
		newUser.password = "password";
		
		// Test that we can add a new user
		try {
			newUser.id = this.accountService.register(newUser);
			
			assertTrue(newUser.username.equals("test@register.com"));
			
			UserAccount retrievedUser = this.accountService.getUserDetails(newUser.id);
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		} catch(UserNotFoundException e) {
			fail("User was registed. They should be able to be found.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that trying to add a user with a username that is already in use throws an exception
		try {
			UserAccount newUser2 = new UserAccount(this.accountService.register(newUser));;
			fail("Added two users with same username.");
		} catch (RegistrationException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that IDs are incremented correctly
		UserAccount newUser2 = new UserAccount();
		newUser2.username = "test2@register.com";
		newUser2.password = "password2";
		
		try {
			newUser2.id = this.accountService.register(newUser2);
			
			assertTrue(newUser2.id.equals(newUser.id + 1));

		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testLogin() {
		UserAccount newUser = new UserAccount();
		newUser.username = "test@login.com";
		newUser.password = "password";
		
		try {
			newUser.id = this.accountService.register(newUser);
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that incorrect username stops login
		try {
			UserAccount credentials = new UserAccount();
			credentials.username = "test@incorrect.com";
			credentials.username = "password";
			this.accountService.login(credentials);
			fail("Incorrect username. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that a correct username but incorrect password stops login
		try {
			UserAccount credentials = new UserAccount();
			credentials.username = "test@login.com";
			credentials.username = "incorrectPass";
			this.accountService.login(credentials);
			fail("Incorrect password. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that a correct username and password allows login
		try {
			UserAccount credentials = new UserAccount();
			credentials.username = "test@login.com";
			credentials.password = "password";
			this.accountService.login(credentials);
		} catch(AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} finally {
			this.accountService.logout();
		}
	}
	
	@Test
	public void testLogout() {
		UserAccount newUser = new UserAccount();
		newUser.username = "test@logout.com";
		newUser.password = "password";
		
		// Make sure no one is logged in to start with
		assertFalse(this.accountService.isLoggedIn());
		
		try {
			newUser.id = this.accountService.register(newUser);
			newUser.password = "password";
			
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.accountService.login(newUser);
		} catch (AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		UserAccount retrievedCurrentUser = this.accountService.getCurrentUser();
		
		// Test we can get the current user and that we are logged in
		assertNotNull(retrievedCurrentUser);
		assertTrue(newUser.id.equals(retrievedCurrentUser.id));
		assertTrue(newUser.username.equals(retrievedCurrentUser.username));
		assertTrue(this.accountService.isLoggedIn());
		
		this.accountService.logout();
		
		// Test that logout works
		assertFalse(this.accountService.isLoggedIn());
	}
	
	@Test 
	public void testGetCurrentUser() {
		UserAccount user1 = new UserAccount();
		user1.username = "test@currentUser1.com";
		user1.password = "password1";
		
		UserAccount user2 = new UserAccount();
		user2.username = "test@currentUser2.com";
		user2.password = "password2";
		
		try {
			user1.id = this.accountService.register(user1);
			user2.id = this.accountService.register(user2);
			
			user1.password = "password1";
			user2.password = "password2";
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			// Test logging in with user1 sets user1 as the current user
			this.accountService.login(user1);
			UserAccount retrievedCurrentUser = this.accountService.getCurrentUser();
						
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user1.id));
			assertTrue(retrievedCurrentUser.username.equals(user1.username));
			assertTrue(this.accountService.isLoggedIn());
			
			this.accountService.logout();
			
			// Test logging in with user2 sets user  as the current user
			this.accountService.login(user2);
			retrievedCurrentUser = this.accountService.getCurrentUser();
			
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user2.id));
			assertTrue(retrievedCurrentUser.username.equals(user2.username));
			assertTrue(this.accountService.isLoggedIn());
		} catch (AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		} finally {
			this.accountService.logout();
		}
	}
	
	@Test
	public void testGetUserDetails() {
		UserAccount user1 = new UserAccount();
		user1.username = "test@getUserDetails1.com";
		user1.password = "password1";
		
		UserAccount user2 = new UserAccount();
		user2.username = "test@getUserDetails2.com";
		user2.password = "password2";
		
		try {
			user1.id = this.accountService.register(user1);
			user2.id = this.accountService.register(user2);
		} catch (RegistrationException e) {
			fail("Unique users - should have been registered.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test we can retrieve registered users using their ID
		try {
			UserAccount retrievedUserId1 = this.accountService.getUserDetails(user1.id);
			UserAccount retrievedUserId2 = this.accountService.getUserDetails(user2.id);
			
			assertEquals(user1.id, retrievedUserId1.id);
			assertEquals(user1.username, retrievedUserId1.username);
			
			assertEquals(user2.id, retrievedUserId2.id);
			assertEquals(user2.username, retrievedUserId2.username);
		} catch (UserNotFoundException e) {
			fail("Users were registered so should be able to retrieve details.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that we can get registered users using their username
		try {
			UserAccount retrievedUserId1 = this.accountService.getUserDetails(user1.username);
			UserAccount retrievedUserId2 = this.accountService.getUserDetails(user2.username);
			
			assertEquals(user1.id, retrievedUserId1.id);
			assertEquals(user1.username, retrievedUserId1.username);
			
			assertEquals(user2.id, retrievedUserId2.id);
			assertEquals(user2.username, retrievedUserId2.username);
		} catch (UserNotFoundException e) {
			fail("Users were registered so should be able to retrieve details.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that trying to retrieve unregistered users results in an exception
		try {
			this.accountService.getUserDetails("madeup@username.com");
			fail("User doesn't exist. UserNotFoundException should have been thrown.");
		} catch(UserNotFoundException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.accountService.getUserDetails(-1);
			fail("User doesn't exist. UserNotFoundException should have been thrown.");
		} catch(UserNotFoundException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testRemoveUser() {
		UserAccount user1 = new UserAccount();
		user1.username = "test@removeUser1.com";
		user1.password = "password1";
		
		UserAccount user2 = new UserAccount();
		user2.username = "test@removeUser2.com";
		user2.password = "password2";
		
		// Test we can remove user using their ID
		try {
			user1.id = this.accountService.register(user1);
			this.accountService.getUserDetails(user1.id);
		} catch (RegistrationException e) {
			fail("Unique users - should have been registered.");
		} catch (UserNotFoundException e) {
			fail("User was registered so should be able to be found.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.accountService.removeUser(user1.id);
		} catch(UserNotFoundException e) {
			fail("User was registered so should be able to be removed.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.accountService.getUserDetails(user1.id);
			fail("User was removed so details should not be found");
		} catch(UserNotFoundException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		// Test that we can removed a user using their username
		try {
			user2.id = this.accountService.register(user2);
			this.accountService.getUserDetails(user2.username);
		} catch (RegistrationException e) {
			fail("Unique users - should have been registered.");
		} catch (UserNotFoundException e) {
			fail("User was registered so should be able to be found.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.accountService.removeUser(user2.username);
		} catch(UserNotFoundException e) {
			fail("User was registered so should be able to be removed.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.accountService.getUserDetails(user2.username);
			fail("User was removed so details should not be found");
		} catch(UserNotFoundException e) {
			// Pass
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
}
