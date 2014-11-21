package u1171639.main.controller;

import java.math.BigDecimal;

import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.AuthorisationException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.model.account.User;
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
	
	public long addLot(Lot lot) throws AuthorisationException {
		if(accountService.isLoggedIn()) {
			return this.lotService.addLot(lot);
		} else {
			throw new AuthorisationException("User must be logged in to partake in auction");
		}
	}
	
	public void updateLot(Lot lot) throws AuthorisationException {
		if(accountService.isLoggedIn()) {
			this.lotService.updateLot(lot);
		} else {
			throw new AuthorisationException("User must be logged in to partake in auction");
		}
	}
	
	public void bidForLot(long lotId, BigDecimal amount) {
		if(accountService.isLoggedIn()) {
			this.lotService.bidForLot(lotId, amount, accountService.getCurrentUser());
		}
	}
	
	public void listenForLot(Lot template, Callback<Void, Lot> callback) {
		if(accountService.isLoggedIn()) {
			this.lotService.listenForLot(template, callback);
		}
	}

	public void subscribeToLot(long id, Callback<Void, Lot> callback) {
		this.lotService.subscribeToLot(id, callback);
	}

}
