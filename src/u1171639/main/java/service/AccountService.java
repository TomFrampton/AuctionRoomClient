package u1171639.main.java.service;

import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.UserAccount;

public interface AccountService {
	public void login(UserAccount credentials) throws AuthenticationException;
	public void logout();
	public boolean isLoggedIn();
	public UserAccount getCurrentUser();
	public long register(UserAccount newUser) throws RegistrationException;
	public void removeUser(long userId) throws UserNotFoundException;
	public void removeUser(String username) throws UserNotFoundException;
	public UserAccount getUserDetails(long userId) throws UserNotFoundException;
	public UserAccount getUserDetails(String username) throws UserNotFoundException;
}
