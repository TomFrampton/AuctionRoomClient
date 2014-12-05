package u1171639.main.java.utilities;

import java.rmi.RemoteException;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.transaction.TransactionException;

public interface TransactionTask<S,T> {
	public T execute(S param) throws RemoteException, TransactionException, UnusableEntryException, InterruptedException;
}
