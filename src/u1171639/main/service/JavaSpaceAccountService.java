package u1171639.main.service;

import u1171639.main.exception.RegistrationException;
import u1171639.main.model.account.User;

public class JavaSpaceAccountService implements AccountService {

	@Override
	public User login(User credentials) {
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

}
