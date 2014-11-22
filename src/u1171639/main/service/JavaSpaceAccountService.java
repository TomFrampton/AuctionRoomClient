package u1171639.main.service;

import net.jini.space.JavaSpace;
import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.model.account.User;
import u1171639.main.utilities.PasswordHashScheme;

public class JavaSpaceAccountService implements AccountService {
	private JavaSpace space;
	private PasswordHashScheme hashScheme;
	
	public JavaSpaceAccountService(JavaSpace space, PasswordHashScheme hashScheme) {
		this.space = space;
		this.hashScheme = hashScheme;
	}
	
	@Override
	public User login(User credentials) throws AuthenticationException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void logout() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public User getCurrentUser() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User register(User newUser) throws RegistrationException {
		// Validate the user 
		return null;
	}

	@Override
	public boolean isLoggedIn() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public User getUserDetails(long userId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public User getUserDetails(String email) {
		// TODO Auto-generated method stub
		return null;
	}

}
