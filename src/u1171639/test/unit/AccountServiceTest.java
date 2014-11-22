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

//	public User login(User credentials) throws AuthenticationException;
//	public void logout();
//	public boolean isLoggedIn();
//	public User getCurrentUser();
//	public User register(User newUser) throws RegistrationException;
	
	@Test
	public void testRegister() {
		User newUser = new User();
		newUser.email = "test@register.com";
		newUser.password = "password";
		
		try {
			User registeredUser = this.accountService.register(newUser);
			
			assertTrue(registeredUser.email.equals(newUser.email));
			assertTrue(registeredUser.password.equals(this.hashScheme.hashPassword(newUser.password, newUser.salt)));
		} catch (RegistrationException e) {
			fail("Unique user - Should have been added.");
		}
		
		
	}
}
