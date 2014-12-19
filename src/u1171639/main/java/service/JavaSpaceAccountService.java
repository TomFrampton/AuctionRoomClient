package u1171639.main.java.service;

import java.rmi.RemoteException;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.UserAccount;
import u1171639.main.java.utilities.PasswordHashScheme;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.TransactionUtils;
import u1171639.main.java.utilities.counters.UserIDCounter;

public class JavaSpaceAccountService implements AccountService {
	private JavaSpace space;
	private PasswordHashScheme hashScheme;
	private TransactionManager transMgr;
	
	private UserAccount loggedInUser;
	
	public JavaSpaceAccountService(JavaSpace space, PasswordHashScheme hashScheme, TransactionManager transMgr) {
		this.space = space;
		this.hashScheme = hashScheme;
		this.transMgr = transMgr;
	}
	
	@Override
	public void login(UserAccount credentials) throws AuthenticationException, AuctionCommunicationException {
		try {
			UserAccount registeredUser = (UserAccount) this.space.readIfExists(new UserAccount(credentials.username), null, SpaceConsts.WAIT_TIME);
			if(registeredUser == null) {
				throw new AuthenticationException("Invalid username or password.");
			}
			
			String hashedPass = this.hashScheme.hashPassword(credentials.password, registeredUser.salt);
			if(!hashedPass.equals(registeredUser.password)) {
				throw new AuthenticationException("Invalid username or password.");
			}
			
			// User logged in
			this.loggedInUser = registeredUser;
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			throw new AuctionCommunicationException();
		}
	}

	@Override
	public void logout() {
		this.loggedInUser = null;
	}

	@Override
	public UserAccount getCurrentUser() {
		return this.loggedInUser;
	}

	@Override
	public long register(UserAccount newUser) throws RegistrationException, AuctionCommunicationException {
		UserAccount template = new UserAccount();
		template.username = newUser.username;
		
		try {
			UserAccount sameUsernameUser = (UserAccount) this.space.readIfExists(template, null, SpaceConsts.WAIT_TIME);
			if(sameUsernameUser != null) {
				throw new RegistrationException("username already in use.");
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			throw new AuctionCommunicationException();
		}
		
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			newUser.salt = this.hashScheme.generateSalt();
			newUser.password = this.hashScheme.hashPassword(newUser.password, newUser.salt);
			
			UserIDCounter counter = (UserIDCounter) this.space.take(new UserIDCounter(), transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
		
			newUser.id = counter.id;
			counter.increment();
		
			this.space.write(counter, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			this.space.write(newUser, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			
			TransactionUtils.commit(transaction);
			
			newUser.salt = null;
			newUser.password = null;
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
		
		return newUser.id;
	}

	@Override
	public boolean isLoggedIn() {
		return this.loggedInUser != null;
	}

	@Override
	public UserAccount getUserDetails(long userId) throws UserNotFoundException, AuctionCommunicationException {
		return this.getUserDetails(new UserAccount(userId));
	}

	@Override
	public UserAccount getUserDetails(String username) throws UserNotFoundException, AuctionCommunicationException {
		return this.getUserDetails(new UserAccount(username));
	}
	
	private UserAccount getUserDetails(UserAccount template) throws UserNotFoundException, AuctionCommunicationException {
		UserAccount retrievedUser = null;
		
		try {
			retrievedUser = (UserAccount) this.space.readIfExists(template, null, SpaceConsts.WAIT_TIME);
			if(retrievedUser == null) {
				throw new UserNotFoundException("That user does not exist.");
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			throw new AuctionCommunicationException();
		}
		
		retrievedUser.password = null;
		retrievedUser.salt = null;
		
		return retrievedUser;
	}

	@Override
	public void removeUser(long userId) throws UserNotFoundException, AuctionCommunicationException {
		this.removeUser(new UserAccount(userId));
	}

	@Override
	public void removeUser(String username) throws UserNotFoundException, AuctionCommunicationException {
		this.removeUser(new UserAccount(username));
	}
	
	private void removeUser(UserAccount template) throws UserNotFoundException, AuctionCommunicationException {
		try {
			UserAccount removedUser = (UserAccount) this.space.takeIfExists(template, null, SpaceConsts.WAIT_TIME);
			if(removedUser == null) {
				throw new UserNotFoundException("User not found.");
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			throw new AuctionCommunicationException();
		}
	}

}
