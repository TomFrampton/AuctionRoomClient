package u1171639.main.service;

import java.rmi.RemoteException;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;
import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.exception.UserNotFoundException;
import u1171639.main.model.account.User;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.PasswordHashScheme;
import u1171639.main.utilities.UserIDCounter;

public class JavaSpaceAccountService implements AccountService {
	private JavaSpace space;
	private PasswordHashScheme hashScheme;
	
	private User loggedInUser;
	
	public JavaSpaceAccountService(JavaSpace space, PasswordHashScheme hashScheme) {
		this.space = space;
		this.hashScheme = hashScheme;
	}
	
	@Override
	public void login(User credentials) throws AuthenticationException {
		try {
			User registeredUser = (User) this.space.readIfExists(new User(credentials.email), null, 0);
			if(registeredUser == null) {
				throw new AuthenticationException("Invalid email or password.");
			}
			
			String hashedPass = this.hashScheme.hashPassword(credentials.password, registeredUser.salt);
			if(!hashedPass.equals(registeredUser.password)) {
				throw new AuthenticationException("Invalid email or password.");
			}
			
			// User logged in
			this.loggedInUser = registeredUser;
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnusableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void logout() {
		this.loggedInUser = null;
	}

	@Override
	public User getCurrentUser() {
		return this.loggedInUser;
	}

	@Override
	public long register(User newUser) throws RegistrationException {
		User template = new User();
		template.email = newUser.email;
		
		try {
			// TODO improve this in case user exists but is temporarily taken from space
			User sameEmailUser = (User) this.space.readIfExists(template, null, 0);
			if(sameEmailUser != null) {
				throw new RegistrationException("Email already in use.");
			}
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnusableEntryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransactionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
			
		try {
			String unhashedPass = newUser.password;
			
			newUser.salt = hashScheme.generateSalt();
			newUser.password = hashScheme.hashPassword(newUser.password, newUser.salt);
			
			UserIDCounter counter = (UserIDCounter) space.take(new UserIDCounter(), null, Lease.FOREVER);
		
			newUser.id = counter.id;
			counter.increment();
		
			space.write(counter, null, Lease.FOREVER);
			space.write(newUser, null, Lease.FOREVER);
			
			newUser.password = unhashedPass;
		} catch (RemoteException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (UnusableEntryException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (TransactionException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (InterruptedException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		return newUser.id;
	}

	@Override
	public boolean isLoggedIn() {
		return this.loggedInUser != null;
	}

	@Override
	public User getUserDetails(long userId) throws UserNotFoundException {
		return this.getUserDetails(new User(userId));
	}

	@Override
	public User getUserDetails(String email) throws UserNotFoundException {
		return this.getUserDetails(new User(email));
	}
	
	private User getUserDetails(User template) throws UserNotFoundException {
		User retrievedUser = null;
		
		try {
			// TODO improve this in case user is temporarily taken from space
			retrievedUser = (User) this.space.readIfExists(template, null, 0);
			if(retrievedUser == null) {
				throw new UserNotFoundException("That user does not exist.");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnusableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return retrievedUser;
	}

	@Override
	public void removeUser(long userId) throws UserNotFoundException {
		this.removeUser(new User(userId));
	}

	@Override
	public void removeUser(String email) throws UserNotFoundException {
		this.removeUser(new User(email));
	}
	
	private void removeUser(User template) throws UserNotFoundException {
		try {
			User removedUser = (User) this.space.takeIfExists(template, null, 0);
			if(removedUser == null) {
				throw new UserNotFoundException("User not found.");
			}
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnusableEntryException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
