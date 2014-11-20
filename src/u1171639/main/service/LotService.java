package u1171639.main.service;

import java.math.BigDecimal;

import u1171639.main.model.account.User;
import u1171639.main.model.lot.Lot;
import u1171639.main.utilities.Callback;

public interface LotService {
	public long addLot(Lot lot);
	public void bidForLot(long id, BigDecimal amount, User bidder);
	public Lot getLotDetails(long id);
	public void subscribeToLot(long id, Callback<Void, Lot> callback);
	public void listenForLot(Lot template, Callback<Void, Lot> callback);
}
