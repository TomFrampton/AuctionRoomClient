package u1171639.main.java.service;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.UserAccount;

public interface AccountService {
	public void login(UserAccount credentials) throws AuthenticationException, AuctionCommunicationException;
	public void logout();
	public boolean isLoggedIn();
	public UserAccount getCurrentUser();
	public long register(UserAccount newUser) throws RegistrationException, AuctionCommunicationException;
	public void removeUser(long userId) throws UserNotFoundException, AuctionCommunicationException;
	public void removeUser(String username) throws UserNotFoundException, AuctionCommunicationException;
	public UserAccount getUserDetails(long userId) throws UserNotFoundException, AuctionCommunicationException;
	public UserAccount getUserDetails(String username) throws UserNotFoundException, AuctionCommunicationException;
}
