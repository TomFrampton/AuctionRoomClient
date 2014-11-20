package u1171639.main.service;

import u1171639.main.model.Lot;
import u1171639.main.utilities.Callback;

public interface LotService {
	public long addLot(Lot lot);
	public Lot getLotDetails(long id);
	public void subscribeToLot(long id, Callback<Void, Lot> callback);
	public void listenForLot(Lot template, Callback<Void, Lot> callback);
}
