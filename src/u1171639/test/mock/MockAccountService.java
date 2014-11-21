package u1171639.test.mock;

import java.util.Hashtable;
import java.util.List;

import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.model.account.User;
import u1171639.main.service.AccountService;

public class MockAccountService implements AccountService {

	private Hashtable<String, User> registeredUsers = new Hashtable<String, User>();
	private User currentUser = null;
	
	@Override
	public User login(User credentials) throws AuthenticationException {
		if(registeredUsers.containsKey(credentials.email)) {
			User identifiedUser = registeredUsers.get(credentials.email);
			
			if(identifiedUser.password.equals(credentials.password)) {
				this.currentUser = identifiedUser;
				return this.currentUser;
			}
		}
		
		throw new AuthenticationException("Invalid email or password");
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
		if(!registeredUsers.containsKey(newUser.email)) {
			registeredUsers.put(newUser.email, newUser);
		} else {
			throw new RegistrationException("Email already in use.");
		}
		
		return newUser;
	}

	@Override
	public boolean isLoggedIn() {
		return this.currentUser != null;
	}

}
