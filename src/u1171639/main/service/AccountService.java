package u1171639.main.service;

import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.model.account.User;

public interface AccountService {
	public User login(User credentials) throws AuthenticationException;
	public void logout();
	public boolean isLoggedIn();
	public User getCurrentUser();
	public User register(User newUser) throws RegistrationException;
	public User getUserDetails(long userId);
	public User getUserDetails(String email);
}
