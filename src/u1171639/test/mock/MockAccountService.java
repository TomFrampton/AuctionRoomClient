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
	private long idCounter = 0;
	
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
		this.currentUser = null;
	}

	@Override
	public User getCurrentUser() {
		return this.currentUser;
	}

	@Override
	public User register(User newUser) throws RegistrationException {
		if(!registeredUsers.containsKey(newUser.email)) {
			newUser.id = this.idCounter++;
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
