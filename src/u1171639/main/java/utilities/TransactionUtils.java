package u1171639.main.java.utilities;

import java.rmi.RemoteException;

import u1171639.main.java.exception.AuctionCommunicationException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.CannotAbortException;
import net.jini.core.transaction.CannotCommitException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.UnknownTransactionException;
import net.jini.core.transaction.server.TransactionManager;

public class TransactionUtils {

	public static Transaction create(TransactionManager transMgr) throws AuctionCommunicationException {
		Transaction.Created trc = null;
		
		try {
			trc = TransactionFactory.create(transMgr, SpaceConsts.TRANSACTION_TIME);
			Transaction transaction = trc.transaction;
			return transaction;
			
		} catch(RemoteException | LeaseDeniedException e) {
			throw new AuctionCommunicationException();
		}
	}
	
	public static void abort(Transaction transaction) throws AuctionCommunicationException {
		try {
			transaction.abort();
		} catch (UnknownTransactionException | RemoteException | CannotAbortException e) {
			throw new AuctionCommunicationException();
		}
	}
	
	public static void commit(Transaction transaction) throws AuctionCommunicationException {
		try {
			transaction.commit();
		} catch (UnknownTransactionException | CannotCommitException | RemoteException e) {
			TransactionUtils.abort(transaction);
		}
	}
}
