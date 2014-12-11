package u1171639.main.java.service;

import java.util.List;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.UserNotification;
import u1171639.main.java.utilities.Callback;

public interface NotificationService {
	
	public List<UserNotification> retrieveAllNotifications(long userId) throws AuctionCommunicationException;;
	
	public void listenForNotifications(long userId, Callback<UserNotification, Void> callback) throws AuctionCommunicationException;
	
	public void addNotification(UserNotification notification) throws AuctionCommunicationException;
	
	public void markNotificationRead(long id) throws NotificationNotFoundException, AuctionCommunicationException;
}
