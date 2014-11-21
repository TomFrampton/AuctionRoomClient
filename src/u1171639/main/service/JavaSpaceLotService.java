package u1171639.main.service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;
import u1171639.main.model.account.User;
import u1171639.main.model.lot.Lot;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.HighestBid;
import u1171639.main.utilities.LotIDCounter;

public class JavaSpaceLotService implements LotService {
	private final JavaSpace space;
	
	public JavaSpaceLotService(JavaSpace space) {
		this.space = space;
	}
	
	@Override
	public long addLot(Lot lot) {
		try {
			// Create a highest bid tracker for this lot
			HighestBid highestBid = new HighestBid();
			highestBid.bidId = HighestBid.NO_BID_ID;
			
			LotIDCounter counter = (LotIDCounter) space.take(new LotIDCounter(), null, Lease.FOREVER);
			
			lot.id = counter.id;
			highestBid.lotId = lot.id;
			counter.increment();
			
			space.write(counter, null, Lease.FOREVER);
			space.write(lot, null, Lease.FOREVER);
			space.write(highestBid, null, Lease.FOREVER);
			
			return lot.id;
			
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public void bidForLot(long id, BigDecimal amount, User bidder) {
		Lot lot = this.getLotDetails(id);
		
		if(lot.sellerId != bidder.id) {
			try {
				// Get current bid info
				// Make bid for lot
				// Add bid to JS
			} catch(Exception e) {
				
			}
		} else {
			// throw some InvalidBidException
		}
	}

	@Override
	public Lot getLotDetails(long id) {
		Lot template = new Lot(id);
		try {
			return (Lot) space.readIfExists(template, null, 0);
		} catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public void subscribeToLot(long id, final Callback<Void, Lot> callback) {
		
		final Lot template = new Lot(id);
		
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
