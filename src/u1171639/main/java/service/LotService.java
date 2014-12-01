package u1171639.main.java.service;

import java.math.BigDecimal;
import java.util.List;

import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;

public interface LotService {
	public long addLot(Lot lot);
	public List<Lot> searchLots(Lot template);
	public List<Lot> getUsersLots(long userId);
	public void updateLot(Lot lot);
	public void bidForLot(long lotId, BigDecimal amount, long bidderId, boolean isPrivateBid) throws UnauthorisedBidException, InvalidBidException;
	public Bid getHighestBid(long lotId);
	public Lot getLotDetails(long lotId);
	public List<Bid> getVisibleBids(long lotId, long userId);
	public void subscribeToLot(long lotId, Callback<Lot, Void> callback);
	public void listenForLot(Lot template, Callback<Lot, Void> callback);
}
