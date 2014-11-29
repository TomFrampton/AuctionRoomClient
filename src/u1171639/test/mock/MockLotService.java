package u1171639.test.mock;

import java.math.BigDecimal;
import java.util.List;

import u1171639.main.java.model.account.User;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.service.LotService;
import u1171639.main.java.utilities.Callback;

public class MockLotService implements LotService {

	@Override
	public long addLot(Lot lot) {
		// TODO Auto-generated method stub
		return 0;
	}
	
	@Override
	public List<Lot> searchLots(Lot template) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void updateLot(Lot lot) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void bidForLot(long lotId, BigDecimal amount, long bidderId) {
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
	public void subscribeToLot(long lotId, Callback<Lot, Void> callback) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void listenForLot(Lot template, Callback<Lot, Void> callback) {
		// TODO Auto-generated method stub
		
	}

}
