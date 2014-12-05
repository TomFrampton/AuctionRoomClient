package u1171639.main.java.service;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.LotSubscription;

public class JavaSpaceNotificationService implements NotificationService {
	private JavaSpace05 space;
	private TransactionManager transMgr;
	
	public JavaSpaceNotificationService(JavaSpace space, TransactionManager transMgr) {
		this.space = (JavaSpace05) space;
	}

	@Override
	public void listenForNotifications(long userId, Callback<Notification, Void> callback) throws NotificationException, LotNotFoundException, AuctionCommunicationException {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void addNotification(Notification notification) throws AuctionCommunicationException {
		// TODO Auto-generated method stub
		
	}

}
