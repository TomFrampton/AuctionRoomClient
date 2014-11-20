package u1171639.main.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;
import u1171639.main.model.Lot;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.LotIDCounter;

public class JavaSpaceLotService implements LotService {
	private final JavaSpace space;
	
	public JavaSpaceLotService(JavaSpace space) {
		this.space = space;
	}
	
	@Override
	public long addLot(Lot lot) {
		try {
			LotIDCounter counter = (LotIDCounter) space.take(new LotIDCounter(), null, Lease.FOREVER);
			
			lot.id = counter.id;
			counter.increment();
			
			space.write(lot, null, Lease.FOREVER);
			space.write(counter, null, Lease.FOREVER);
			
			return lot.id;
			
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}

	@Override
	public Lot getLotDetails(long id) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribeToLot(long id, Callback<Void, Lot> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void listenForLot(final Lot template, final Callback<Void, Lot> callback) {
		RemoteEventListener listener = new RemoteEventListener() {
			
			@Override
			public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
				try {
					Lot lot = (Lot) space.readIfExists(template, null, 0);
					if(lot != null) {
						callback.call(lot);
					}
				} catch(RemoteException e) {
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
		};
		
		try {
			UnicastRemoteObject.exportObject(listener,0);
			space.notify(template, null, listener, Lease.FOREVER, null);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
