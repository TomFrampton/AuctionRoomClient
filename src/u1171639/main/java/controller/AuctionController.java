package u1171639.main.java.controller;

import java.util.List;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;

public interface AuctionController {
	public void launch();
	public long register(User newUser) throws RegistrationException;
	public void login(User credentials) throws AuthenticationException;
	public void logout();
	public boolean isLoggedIn();
	public User getCurrentUser();
	public User getUserDetails(long userId) throws UserNotFoundException;
	public User getUserDetails(String email) throws UserNotFoundException;
	public void removeUser(long userId) throws UserNotFoundException;
	public void removeUser(String email) throws UserNotFoundException;
	public long addLot(Lot lot, Callback<Bid, Void> bidCallback) throws RequiresLoginException, AuctionCommunicationException;
	public List<Lot> searchLots(Lot template) throws RequiresLoginException, AuctionCommunicationException;
	public List<Lot> getUsersLots() throws RequiresLoginException, AuctionCommunicationException;
	public void updateLot(Lot lot) throws RequiresLoginException, AuctionCommunicationException;
	public void bidForLot(Bid bid) throws RequiresLoginException, UnauthorisedBidException, InvalidBidException, LotNotFoundException, AuctionCommunicationException;
	public Bid getHighestBid(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException;
	public List<Bid> getVisibleBids(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException;
	public void listenForLot(Lot template, Callback<Lot, Void> callback) throws RequiresLoginException;
	public void subscribeToLot(long id, Callback<Lot, Void> callback) throws RequiresLoginException, NotificationException, LotNotFoundException;
	public Lot getLotDetails(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException;
	public void removeLot(long lotId) throws UnauthorisedLotActionException, LotNotFoundException, RequiresLoginException, AuctionCommunicationException;
}
