package u1171639.main.controller;

import u1171639.main.model.lot.Car;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.view.AuctionView;

public class AuctionController {
	private AuctionView view;
	private LotService lotService;
	
	public AuctionController(AuctionView view, LotService lotService) {
		this.view = view;
		this.lotService = lotService;
		this.view.init(this);
	}
	
	public long addLot(Lot lot) {
		return this.lotService.addLot(lot);
	}
	
	public void listenForLot(Lot template, Callback<Void, Lot> callback) {
		this.lotService.listenForLot(template, callback);
	}

	public void subscribeToLot(long id, Callback<Void, Lot> callback) {
		this.lotService.subscribeToLot(id, callback);
	}

}
