package u1171639.test.unit;

import static org.junit.Assert.*;

import java.net.ConnectException;

import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.model.account.User;
import u1171639.main.service.AccountService;
import u1171639.main.service.JavaSpaceAccountService;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.MediumSecurityHashScheme;
import u1171639.main.utilities.PasswordHashScheme;
import u1171639.main.utilities.SpaceUtils;
import u1171639.main.utilities.UserIDCounter;

public class AccountServiceTest {
	private JavaSpaceAccountService accountService;
	private PasswordHashScheme hashScheme;
	
	@Before
	public void setUp() throws Exception {
		JavaSpace space = SpaceUtils.getSpace("localhost");
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		this.hashScheme = new MediumSecurityHashScheme();
		this.accountService = new JavaSpaceAccountService(space, this.hashScheme);
		UserIDCounter.initialiseInSpace(space);
	}

	@After
	public void tearDown() throws Exception {
	}
	
	@Test
	public void testRegister() {
		User newUser = new User();
		newUser.email = "test@register.com";
		newUser.password = "password";
		
		User registeredUser = null;
		// Test that we can add a new user
		try {
			registeredUser = this.accountService.register(newUser);
			
			assertTrue(registeredUser.email.equals(newUser.email));
			assertTrue(registeredUser.password.equals(this.hashScheme.hashPassword(newUser.password, newUser.salt)));
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		}
		
		// Test that trying to add a user with a email that is already in use throws an exception
		try {
			this.accountService.register(newUser);
			fail("Added two users with same email.");
		} catch (RegistrationException e) {
			// Pass
		}
		
		// Test that IDs are incremented correctly
		User newUser2 = new User();
		newUser2.email = "test2@register.com";
		newUser2.password = "password2";
		
		try {
			User registeredUser2 = this.accountService.register(newUser2);
			
			assertTrue(registeredUser2.id.equals(registeredUser.id + 1));

		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		}
	}
	
	@Test
	public void testLogin() {
		User newUser = new User();
		newUser.email = "test@login.com";
		newUser.password = "password";
		
		try {
			this.accountService.register(newUser);
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		}
		
		// Test that incorrect email stops login
		try {
			User credentials = new User();
			credentials.email = "test@incorrect.com";
			credentials.email = "password";
			this.accountService.login(credentials);
			fail("Incorrect email. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		}
		
		// Test that a correct email but incorrect password stops login
		try {
			User credentials = new User();
			credentials.email = "test@login.com";
			credentials.email = "incorrectPass";
			this.accountService.login(credentials);
			fail("Incorrect password. User should not have been able to log in");
		} catch(AuthenticationException e) {
			// Pass
		}
		
		// Test that a correct email and password allows login
		try {
			User credentials = new User();
			credentials.email = "test@login.com";
			credentials.email = "password";
			this.accountService.login(credentials);
		} catch(AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} finally {
			this.accountService.logout();
		}
	}
	
	@Test
	public void testLogout() {
		User newUser = new User();
		newUser.email = "test@logout.com";
		newUser.password = "password";
		
		User currentUser = null;
		// Make sure no one is logged in to start with
		assertFalse(this.accountService.isLoggedIn());
		
		try {
			this.accountService.register(newUser);
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		}
		
		try {
			currentUser = this.accountService.login(newUser);
		} catch (AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		}
		
		User retrievedCurrentUser = this.accountService.getCurrentUser();
		
		// Test we can get the current user and that we are logged in
		assertNotNull(retrievedCurrentUser);
		assertTrue(currentUser.id.equals(retrievedCurrentUser.id));
		assertTrue(currentUser.email.equals(retrievedCurrentUser.email));
		assertTrue(this.accountService.isLoggedIn());
		
		this.accountService.logout();
		
		// Test that logout works
		assertFalse(this.accountService.isLoggedIn());
	}
	
	@Test 
	public void testGetCurrentUser() {
		User user1 = new User();
		user1.email = "test@currentUser1.com";
		user1.password = "password1";
		
		User user2 = new User();
		user2.email = "test@currentUser1.com";
		user2.password = "password";
		
		try {
			this.accountService.register(user1);
			this.accountService.register(user2);
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		}
		
		try {
			// Test logging in with user1 sets user1 as the current user
			this.accountService.login(user1);
			User retrievedCurrentUser = this.accountService.getCurrentUser();
						
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user1.id));
			assertTrue(retrievedCurrentUser.email.equals(user1.email));
			assertTrue(this.accountService.isLoggedIn());
			
			this.accountService.logout();
			
			// Test logging in with user2 sets user  as the current user
			this.accountService.login(user2);
			retrievedCurrentUser = this.accountService.getCurrentUser();
			
			assertNotNull(retrievedCurrentUser);
			assertTrue(retrievedCurrentUser.id.equals(user2.id));
			assertTrue(retrievedCurrentUser.email.equals(user2.email));
			assertTrue(this.accountService.isLoggedIn());
		} catch (AuthenticationException e) {
			fail("Credentials were correct. User should have been able to log in");
		} finally {
			this.accountService.logout();
		}
	}
}
