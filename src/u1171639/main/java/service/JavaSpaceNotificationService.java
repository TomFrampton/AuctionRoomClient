package u1171639.main.java.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.TransactionUtils;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.main.java.utilities.counters.NotificationIDCounter;
import u1171639.main.java.utilities.flags.LotAddedFlag;
import u1171639.main.java.utilities.flags.NotificationAddedFlag;

public class JavaSpaceNotificationService implements NotificationService {
	private JavaSpace05 space;
	private TransactionManager transMgr;
	
	public JavaSpaceNotificationService(JavaSpace space, TransactionManager transMgr) {
		this.space = (JavaSpace05) space;
		this.transMgr = transMgr;
	}

	@Override
	public List<Notification> retrieveAllNotifications(long userId) throws AuctionCommunicationException {
		List<Notification> retrievedNotifications = new ArrayList<Notification>();
		
		Notification template = new Notification();
		template.recipientId = userId;
		
		// Create a transaction - we will abort later to return the listed
		// notification objects to the space
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			// We will use this template multiple times.
			Entry snapshot = this.space.snapshot(template);
			
			// Keep taking notifications out until there are no more that match the template
			boolean notificationsToTake = true;
			while(notificationsToTake) {
					Notification retrievedNotification = (Notification) this.space.takeIfExists(snapshot, transaction, SpaceConsts.WAIT_TIME);
					
					if(retrievedNotification != null) {
						retrievedNotifications.add(retrievedNotification);
					} else {
						// No more notifications. Abort the transaction to return them to the space.
						notificationsToTake = false;
						TransactionUtils.abort(transaction);
					}
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			// Something went wrong. Abort transaction to roll back.
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
		
		return retrievedNotifications;
	}
	
	@Override
	public void listenForNotifications(long userId, final Callback<Notification, Void> callback) throws AuctionCommunicationException {
		List<NotificationAddedFlag> templates = new ArrayList<NotificationAddedFlag>();
		
		// Create template to match any notification flags for this user
		NotificationAddedFlag flagTemplate = new NotificationAddedFlag();
		flagTemplate.recipientId = userId;
		
		templates.add(flagTemplate);
		
		RemoteEventListener listener = new RemoteEventListener() {
			@Override
			public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
				AvailabilityEvent event = (AvailabilityEvent) theEvent;
				
				try {
					// Get the notification flag that triggered this exception
					NotificationAddedFlag flag = (NotificationAddedFlag) event.getEntry();
					
					// Use the flag to read the notification itself
					Notification template = new Notification();
					template.id = flag.notificationId;
					
					Notification retrievedNotification = (Notification) space.readIfExists(template, null, SpaceConsts.WAIT_TIME);
					
					if(retrievedNotification != null && callback != null) {
						callback.call(retrievedNotification);
					}
					
				} catch (UnusableEntryException | TransactionException | InterruptedException e) {
					// Something went wrong. Don't call the callback.
				}
				
			}	
		};
		
		try {
			UnicastRemoteObject.exportObject(listener, 0);
			this.space.registerForAvailabilityEvent(templates, null, false, listener, SpaceConsts.AUCTION_ENTITY_WRITE_TIME, null);
		} catch(RemoteException | TransactionException e) {
			throw new AuctionCommunicationException();
		}
	}

	@Override
	public void addNotification(Notification notification) throws AuctionCommunicationException {
		// Create a transaction
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			// Get the notification counter so we can assign a unique ID for this Notification. Take it from the space to enforce
			// mutual exclusion when incrementing and assigning the ID number.
			NotificationIDCounter counter = (NotificationIDCounter) this.space.takeIfExists(new NotificationIDCounter(), transaction, SpaceConsts.WAIT_TIME);
			
			if(counter == null) {
				throw new AuctionCommunicationException();
			}
			
			notification.id = counter.id;
			counter.increment();
			
			notification.timeReceived = new Date(System.currentTimeMillis());
			notification.read = false;
			
			// Add the flag to announce that we have added a Notification in the space
			NotificationAddedFlag notificationAddedFlag = new NotificationAddedFlag();
			notificationAddedFlag.notificationId = notification.id;
			notificationAddedFlag.recipientId = notification.recipientId;
			
			this.space.write(notification, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			this.space.write(counter, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			
			this.space.write(notificationAddedFlag, transaction, SpaceConsts.FLAG_WRITE_TIME);
			
			TransactionUtils.commit(transaction);
			
			// Something went wrong. Abort the transaction.
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
	}

	@Override
	public void markNotificationRead(long id) throws NotificationNotFoundException, AuctionCommunicationException {
		// Take the notification that we need to update
		Notification template = new Notification(id);
		
		Transaction transaction = TransactionUtils.create(this.transMgr);
			
		try {
			Notification notification = (Notification) this.space.takeIfExists(template, transaction, SpaceConsts.WAIT_TIME);
			if(notification == null) {
				TransactionUtils.abort(transaction);
				throw new NotificationNotFoundException("Notification with that ID could not be found.");
			}
			
			notification.read = true;
			
			this.space.write(notification, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			
			TransactionUtils.commit(transaction);
			
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
	}

}
