package u1171639.main.java.service;

import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.User;

public interface AccountService {
	public void login(User credentials) throws AuthenticationException;
	public void logout();
	public boolean isLoggedIn();
	public User getCurrentUser();
	public long register(User newUser) throws RegistrationException;
	public void removeUser(long userId) throws UserNotFoundException;
	public void removeUser(String email) throws UserNotFoundException;
	public User getUserDetails(long userId) throws UserNotFoundException;
	public User getUserDetails(String email) throws UserNotFoundException;
}
