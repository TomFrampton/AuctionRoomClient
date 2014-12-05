package u1171639.main.java.utilities;

import java.rmi.RemoteException;

import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionManager;

public class TransactionUtils {

	public static Transaction create(TransactionManager transMgr) {
		Transaction.Created trc = null;
		
		try {
			trc = TransactionFactory.create(transMgr, Lease.FOREVER);
		} catch(RemoteException | LeaseDeniedException e) {
			// TODO
			return null;
		}
		
		Transaction transaction = trc.transaction;
		return transaction;
	}
	
	public static void abort(Transaction transaction) {
		try {
			transaction.abort();
		} catch (UnknownTransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (CannotAbortException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public static void commit(Transaction transaction) {
		try {
			transaction.commit();
		} catch (UnknownTransactionException | CannotCommitException | RemoteException e) {
			TransactionUtils.abort(transaction);
		}
	}
}
