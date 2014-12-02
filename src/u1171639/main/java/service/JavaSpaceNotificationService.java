package u1171639.main.java.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Hashtable;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.NotificationSubscription;

public class JavaSpaceNotificationService implements NotificationService {
	private JavaSpace space;
	private TransactionManager transMgr;
	
	public JavaSpaceNotificationService(JavaSpace space, TransactionManager transMgr) {
		this.space = space;
	}
	
	@Override
	public void subscribeToLot(long lotId, long userId, final Callback<Lot, Void> callback) throws NotificationException, LotNotFoundException {
		
		try {
			Lot lot = (Lot) this.space.readIfExists(new Lot(lotId), null, 0);
			if(lot == null) {
				throw new LotNotFoundException("That Lot was not found.");
			}
			
			NotificationSubscription notificationTemplate = new NotificationSubscription(lotId, userId);
			NotificationSubscription subscription = (NotificationSubscription) this.space.readIfExists(notificationTemplate, null, 0);
			
			if(subscription != null) {
				throw new NotificationException("You are already subscribed to notifications on this Lot.");
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
			
		
		final Lot template = new Lot(lotId);
		
		RemoteEventListener listener = new RemoteEventListener() {
			
			@Override
			public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
				try {
					Lot lot = (Lot) JavaSpaceNotificationService.this.space.readIfExists(template, null, 0);
					
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
			this.space.write(new NotificationSubscription(lotId, userId), null, Lease.FOREVER);
			this.space.notify(template, null, listener, Lease.FOREVER, null);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void listenForLot(final Lot template, long userId, final Callback<Lot, Void> callback) {
		RemoteEventListener listener = new RemoteEventListener() {
			
			@Override
			public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
				try {
					Lot lot = (Lot) JavaSpaceNotificationService.this.space.readIfExists(template, null, 0);
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
			this.space.notify(template, null, listener, Lease.FOREVER, null);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
