package u1171639.main.controller;

import u1171639.main.model.Lot;
import u1171639.main.service.LotService;
import u1171639.main.view.AuctionView;

public class AuctionController {
	private AuctionView view;
	private LotService lotService;
	
	public AuctionController(AuctionView view, LotService lotService) {
		this.view = view;
		this.lotService = lotService;
		this.view.init(this);
	}
	
	public int addLot(Lot lot) {
		return this.lotService.addLot(lot);
	}

}
