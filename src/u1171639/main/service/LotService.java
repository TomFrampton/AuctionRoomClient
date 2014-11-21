package u1171639.main.service;

import java.math.BigDecimal;

import u1171639.main.model.account.User;
import u1171639.main.model.lot.Bid;
import u1171639.main.model.lot.Lot;
import u1171639.main.utilities.Callback;

public interface LotService {
	public long addLot(Lot lot);
	public void updateLot(Lot lot);
	public void bidForLot(long lotId, BigDecimal amount, User bidder);
	public Bid getHighestBid(long lotId);
	public Lot getLotDetails(long lotId);
	public void subscribeToLot(long lotId, Callback<Void, Lot> callback);
	public void listenForLot(Lot template, Callback<Void, Lot> callback);
}
