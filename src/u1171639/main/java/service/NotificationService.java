package u1171639.main.java.service;

import java.util.List;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.utilities.Callback;

public interface NotificationService {
	
	public List<Notification> retrieveAllNotifications(long userId) throws AuctionCommunicationException;;
	
	public void listenForNotifications(long userId, Callback<Notification, Void> callback) throws AuctionCommunicationException;
	
	public void addNotification(Notification notification) throws AuctionCommunicationException;
	
	public void markNotificationRead(long id) throws NotificationNotFoundException, AuctionCommunicationException;
}
