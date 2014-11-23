package u1171639.main.service;

import java.rmi.RemoteException;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;
import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.RegistrationException;
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
	public User login(User credentials) throws AuthenticationException {
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
		User template = new User();
		template.email = newUser.email;
		
		User registeredUser = null;
		
		try {
			// TODO improve this in case user exists but is temporarily taken from space
			User sameEmailUser = (User) this.space.takeIfExists(template, null, 0);
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
			newUser.salt = hashScheme.generateSalt();
			newUser.password = hashScheme.hashPassword(newUser.password, newUser.salt);
			
			UserIDCounter counter = (UserIDCounter) space.take(new UserIDCounter(), null, Lease.FOREVER);
		
			newUser.id = counter.id;
			counter.increment();
		
			space.write(counter, null, Lease.FOREVER);
			space.write(newUser, null, Lease.FOREVER);
			
			registeredUser = newUser;
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
		
		return registeredUser;
	}

	@Override
	public boolean isLoggedIn() {
		return this.loggedInUser != null;
	}

	@Override
	public User getUserDetails(long userId) {
		User template = new User();
		template.id = userId;
		
		User retrievedUser = null;
		
		try {
			retrievedUser = (User) this.space.readIfExists(template, null, 0);
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
	public User getUserDetails(String email) {
		// TODO Auto-generated method stub
		return null;
	}

}
