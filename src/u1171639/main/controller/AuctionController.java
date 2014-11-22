package u1171639.main.controller;

import java.math.BigDecimal;

import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.InvalidBidException;
import u1171639.main.exception.RequiresLoginException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.exception.UnauthorisedBidException;
import u1171639.main.model.account.User;
import u1171639.main.model.lot.Bid;
import u1171639.main.model.lot.Car;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.AccountService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.view.AuctionView;

public class AuctionController {
	private AuctionView view;
	private LotService lotService;
	private AccountService accountService;
	
	public AuctionController(AuctionView view, LotService lotService, AccountService accountService) {
		this.view = view;
		this.lotService = lotService;
		this.accountService = accountService;
		this.view.init(this);
	}
	
	public User register(User newUser) throws RegistrationException {
		return this.accountService.register(newUser);
	}
	
	public User login(User credentials) throws AuthenticationException {
		return this.accountService.login(credentials);
	}
	
	public void logout() {
		this.accountService.logout();
	}
	
	public long addLot(Lot lot) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			lot.sellerId = this.accountService.getCurrentUser().id;
			return this.lotService.addLot(lot);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	public void updateLot(Lot lot) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			this.lotService.updateLot(lot);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	public void bidForLot(long lotId, BigDecimal amount) throws RequiresLoginException, UnauthorisedBidException, InvalidBidException {
		if(accountService.isLoggedIn()) {
			this.lotService.bidForLot(lotId, amount, accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	public Bid getHighestBid(long lotId) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			return this.lotService.getHighestBid(lotId);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	public void listenForLot(Lot template, Callback<Void, Lot> callback) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			this.lotService.listenForLot(template, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}

	public void subscribeToLot(long id, Callback<Void, Lot> callback) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			this.lotService.subscribeToLot(id, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}

}
