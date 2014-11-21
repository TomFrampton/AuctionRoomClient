package u1171639.test.mock;

import java.math.BigDecimal;

import u1171639.main.model.account.User;
import u1171639.main.model.lot.Bid;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;

public class MockLotService implements LotService {

	@Override
	public long addLot(Lot lot) {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public void updateLot(Lot lot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bidForLot(long lotId, BigDecimal amount, User bidder) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public Bid getHighestBid(long lotId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public Lot getLotDetails(long lotId) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void subscribeToLot(long lotId, Callback<Void, Lot> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void listenForLot(Lot template, Callback<Void, Lot> callback) {
		// TODO Auto-generated method stub
		
	}

}
