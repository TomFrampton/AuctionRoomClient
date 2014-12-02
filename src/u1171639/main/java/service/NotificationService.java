package u1171639.main.java.service;

import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;

public interface NotificationService {
	public void subscribeToLot(long lotId, long userId, Callback<Lot, Void> callback) throws NotificationException, LotNotFoundException;
	public void listenForLot(Lot template, long userId, Callback<Lot, Void> callback);
}
