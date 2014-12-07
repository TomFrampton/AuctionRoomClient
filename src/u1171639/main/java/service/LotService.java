package u1171639.main.java.service;

import java.rmi.RemoteException;
import java.util.List;

import net.jini.core.transaction.TransactionException;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.BidNotFoundException;
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
	
	public long bidForLot(Bid bid) throws UnauthorisedBidException, InvalidBidException, LotNotFoundException, AuctionCommunicationException;
	
	public Bid getHighestBid(long lotId, long userId) throws LotNotFoundException, AuctionCommunicationException;
	
	public Lot getLotDetails(long lotId) throws LotNotFoundException, AuctionCommunicationException;
	
	public Bid getBidDetails(long id) throws AuctionCommunicationException, BidNotFoundException;
	
	public List<Bid> getVisibleBids(long lotId, long userId) throws LotNotFoundException, AuctionCommunicationException;
	
	public void acceptBid(long bidId) throws BidNotFoundException, AuctionCommunicationException;
	
	public void removeLot(long lotId, long userId) throws UnauthorisedLotActionException, LotNotFoundException, AuctionCommunicationException;
	
	public void listenForLotUpdates(long lotId, Callback<Lot, Void> callback) throws NotificationException, LotNotFoundException, AuctionCommunicationException;
	
	public void listenForLotAddition(final Lot template, Callback<Lot, Void> callback) throws AuctionCommunicationException;
	
	public void listenForLotRemoval(long lotId, Callback<Lot, Void> callback) throws AuctionCommunicationException;
	
	public void listenForBidsOnLot(long lotId, Callback<Bid, Void> callback) throws AuctionCommunicationException;
	
	public void listenForAcceptedBidOnLot(long lotId, Callback<Bid, Void> callback) throws AuctionCommunicationException;
}
