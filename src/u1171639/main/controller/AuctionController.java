package u1171639.main.controller;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.List;

import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import u1171639.main.exception.AuthenticationException;
import u1171639.main.exception.InvalidBidException;
import u1171639.main.exception.RequiresLoginException;
import u1171639.main.exception.RegistrationException;
import u1171639.main.exception.UnauthorisedBidException;
import u1171639.main.exception.UserNotFoundException;
import u1171639.main.model.account.User;
import u1171639.main.model.lot.Bid;
import u1171639.main.model.lot.Car;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.AccountService;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.SpaceUtils;
import u1171639.main.view.AuctionView;
import u1171639.test.mock.MockAccountService;

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
	
	public long register(User newUser) throws RegistrationException {
		return this.accountService.register(newUser);
	}
	
	public void login(User credentials) throws AuthenticationException {
		this.accountService.login(credentials);
	}
	
	public void logout() {
		this.accountService.logout();
	}
	
	public boolean isLoggedIn() {
		return this.accountService.isLoggedIn();
	}
	
	public User getCurrentUser() {
		return this.accountService.getCurrentUser();
	}
	
	public User getUserDetails(long userId) throws UserNotFoundException {
		return this.accountService.getUserDetails(userId);
	}
	
	public User getUserDetails(String email) throws UserNotFoundException {
		return this.accountService.getUserDetails(email);
	}
	
	public void removeUser(long userId) throws UserNotFoundException {
		this.accountService.removeUser(userId);
	}
	
	public void removeUser(String email) throws UserNotFoundException {
		this.accountService.removeUser(email);
	}
	
	public long addLot(Lot lot) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			lot.sellerId = this.accountService.getCurrentUser().id;
			return this.lotService.addLot(lot);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	public List<Lot> searchLots(Lot template) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			return this.lotService.searchLots(template);
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
	
	public Lot getLotDetails(long lotId) throws RequiresLoginException {
		if(accountService.isLoggedIn()) {
			return this.lotService.getLotDetails(lotId);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	public static void main(String[] args) throws ConnectException {
		JavaSpace space = SpaceUtils.getSpace("localhost");
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TransactionManager transMgr = SpaceUtils.getManager("localhost");
		if(transMgr == null) {
			throw new ConnectException("Could not connect to TransactionManager");
		}
		
		LotService lotService = new JavaSpaceLotService(space, transMgr);
		AccountService accountService = new MockAccountService();
		
		// TODO
		new AuctionController(null, lotService, accountService);
		
		LotIDCounter.initialiseInSpace(space);
	}

}
