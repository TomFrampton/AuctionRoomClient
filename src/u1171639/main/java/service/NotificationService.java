package u1171639.main.java.service;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.utilities.Callback;

public interface NotificationService {
	public void listenForNotifications(long userId, Callback<Notification, Void> callback) throws NotificationException, LotNotFoundException, AuctionCommunicationException;
	
	public void addNotification(Notification notification) throws AuctionCommunicationException;
}
