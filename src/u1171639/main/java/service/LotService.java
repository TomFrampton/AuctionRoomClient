package u1171639.main.java.service;

import java.util.List;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;

public interface LotService {
	public long addLot(Lot lot, Callback<Bid, Void> notificationCallback) throws AuctionCommunicationException;
	public List<Lot> searchLots(Lot template) throws AuctionCommunicationException;
	public List<Lot> getUsersLots(long userId) throws AuctionCommunicationException;
	public void updateLot(Lot lot) throws AuctionCommunicationException;
	public void bidForLot(Bid bid) throws UnauthorisedBidException, InvalidBidException, LotNotFoundException, AuctionCommunicationException;
	public Bid getHighestBid(long lotId, long userId) throws LotNotFoundException, AuctionCommunicationException;
	public Lot getLotDetails(long lotId) throws LotNotFoundException, AuctionCommunicationException;
	public List<Bid> getVisibleBids(long lotId, long userId) throws LotNotFoundException, AuctionCommunicationException;
	public void removeLot(long lotId, long userId) throws UnauthorisedLotActionException, LotNotFoundException, AuctionCommunicationException;
	public void subscribeToLotUpdates(long lotId, long userId, final Callback<Lot, Void> callback) throws NotificationException, LotNotFoundException, AuctionCommunicationException;
	public void listenForLot(final Lot template, long userId, final Callback<Lot, Void> callback) throws AuctionCommunicationException;
	
}
