package u1171639.test.mock;

import java.util.Hashtable;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.service.AccountService;

public class MockAccountService implements AccountService {
	
	private Hashtable<String, User> registeredUsers = new Hashtable<String, User>();
	private User currentUser = null;
	private long idCounter = 0;
	
	@Override
	public void login(User credentials) throws AuthenticationException {
		if(this.registeredUsers.containsKey(credentials.username)) {
			User identifiedUser = this.registeredUsers.get(credentials.username);
			
			if(identifiedUser.password.equals(credentials.password)) {
				this.currentUser = identifiedUser;
				return;
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
	public long register(User newUser) throws RegistrationException {
		if(!this.registeredUsers.containsKey(newUser.username)) {
			newUser.id = this.idCounter++;
			this.registeredUsers.put(newUser.username, newUser);
		} else {
			throw new RegistrationException("Email already in use.");
		}
		
		return newUser.id;
	}

	@Override
	public boolean isLoggedIn() {
		return this.currentUser != null;
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

	@Override
	public void removeUser(long userId) throws UserNotFoundException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void removeUser(String email) throws UserNotFoundException {
		// TODO Auto-generated method stub
		
	}

}
