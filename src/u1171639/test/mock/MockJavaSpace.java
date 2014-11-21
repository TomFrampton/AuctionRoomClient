package u1171639.test.mock;

import java.rmi.MarshalledObject;
import java.rmi.RemoteException;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.EventRegistration;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

public class MockJavaSpace implements JavaSpace {

	@Override
	public EventRegistration notify(Entry tmpl, Transaction txn,
			RemoteEventListener listener, long lease, MarshalledObject handback)
			throws TransactionException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry read(Entry tmpl, Transaction txn, long timeout)
			throws UnusableEntryException, TransactionException,
			InterruptedException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry readIfExists(Entry tmpl, Transaction txn, long timeout)
			throws UnusableEntryException, TransactionException,
			InterruptedException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry snapshot(Entry e) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry take(Entry tmpl, Transaction txn, long timeout)
			throws UnusableEntryException, TransactionException,
			InterruptedException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Entry takeIfExists(Entry tmpl, Transaction txn, long timeout)
			throws UnusableEntryException, TransactionException,
			InterruptedException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lease write(Entry entry, Transaction txn, long lease)
			throws TransactionException, RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
