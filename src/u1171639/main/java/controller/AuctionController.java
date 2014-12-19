package u1171639.main.java.controller;

import java.util.List;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.BidNotFoundException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.exception.UnauthorisedNotificationActionException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.account.UserAccount;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.utilities.Callback;

public interface AuctionController {
	public void launch();
	
	/* Account methods */
	public long register(UserAccount newUser) throws RegistrationException, ValidationException, AuctionCommunicationException;
	
	public void login(UserAccount credentials) throws AuthenticationException, ValidationException, AuctionCommunicationException;
	
	public void logout();
	
	public boolean isLoggedIn();
	
	public UserAccount getCurrentUser();
	
	public UserAccount getUserDetails(long userId) throws UserNotFoundException, AuctionCommunicationException;
	
	public UserAccount getUserDetails(String username) throws UserNotFoundException, AuctionCommunicationException;
	
	public void removeUser(long userId) throws UserNotFoundException, AuctionCommunicationException;
	
	public void removeUser(String username) throws UserNotFoundException, AuctionCommunicationException;
	
	/* Lot methods */
	public long addLot(Lot lot, Callback<Bid, Void> bidCallback) throws RequiresLoginException, AuctionCommunicationException, ValidationException;
	
	public List<Lot> searchLots(Lot template) throws RequiresLoginException, AuctionCommunicationException, UserNotFoundException;
	
	public List<Lot> getUsersLots() throws RequiresLoginException, AuctionCommunicationException;
	
	public void updateLot(Lot lot) throws RequiresLoginException, AuctionCommunicationException, ValidationException;
	
	public void bidForLot(Bid bid) throws RequiresLoginException, UnauthorisedBidException, InvalidBidException, LotNotFoundException, AuctionCommunicationException, ValidationException;
	
	public void acceptBid(long bidId) throws RequiresLoginException, BidNotFoundException, AuctionCommunicationException;
	
	public Bid getHighestBid(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException;
	
	public List<Bid> getVisibleBids(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException, UserNotFoundException;
	
	public void listenForLotAddition(Lot template, Callback<Lot, Void> callback) throws RequiresLoginException, AuctionCommunicationException;
	
	public void listenForLotUpdates(long id, Callback<Lot, Void> callback) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException;
	
	public void listenForLotRemoval(long lotId, Callback<Lot, Void> callback) throws RequiresLoginException, AuctionCommunicationException;
	
	public void listenForBidsOnLot(long lotId, final Callback<Bid, Void> callback) throws RequiresLoginException, AuctionCommunicationException;
	
	public void listenForAcceptedBidOnLot(long lotId, final Callback<Bid, Void> callback) throws RequiresLoginException, AuctionCommunicationException;
	
	public Lot getLotDetails(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException;
	
	public void removeLot(long lotId) throws UnauthorisedLotActionException, LotNotFoundException, RequiresLoginException, AuctionCommunicationException;
	
	/* Notification  methods */
	public List<Notification> retrieveAllNotifications() throws RequiresLoginException, AuctionCommunicationException;
	
	public void listenForNotifications(Callback<Notification, Void> callback) throws RequiresLoginException, AuctionCommunicationException;
	
	public void addNotification(Notification notification) throws RequiresLoginException, AuctionCommunicationException;
	
	public void markNotificationRead(long notificationId) throws RequiresLoginException, UnauthorisedNotificationActionException, AuctionCommunicationException, NotificationNotFoundException;
}
