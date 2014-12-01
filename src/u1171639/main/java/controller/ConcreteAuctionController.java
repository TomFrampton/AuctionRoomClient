package u1171639.main.java.controller;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.List;

import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.service.AccountService;
import u1171639.main.java.service.JavaSpaceAccountService;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.service.LotService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.LotIDCounter;
import u1171639.main.java.utilities.MediumSecurityHashScheme;
import u1171639.main.java.utilities.PasswordHashScheme;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.UserIDCounter;
import u1171639.main.java.view.AuctionView;
import u1171639.main.java.view.JavaFXAuctionView;

public class ConcreteAuctionController implements AuctionController {
	private AuctionView view;
	private LotService lotService;
	private AccountService accountService;
	
	public ConcreteAuctionController(AuctionView view, LotService lotService, AccountService accountService) {
		this.view = view;
		this.lotService = lotService;
		this.accountService = accountService;
	}
	
	@Override
	public void launch() {
		this.view.start(this);
	}
	
	@Override
	public long register(User newUser) throws RegistrationException {
		return this.accountService.register(newUser);
	}
	
	@Override
	public void login(User credentials) throws AuthenticationException {
		this.accountService.login(credentials);
	}
	
	@Override
	public void logout() {
		this.accountService.logout();
	}
	
	@Override
	public boolean isLoggedIn() {
		return this.accountService.isLoggedIn();
	}
	
	@Override
	public User getCurrentUser() {
		return this.accountService.getCurrentUser();
	}
	
	@Override
	public User getUserDetails(long userId) throws UserNotFoundException {
		return this.accountService.getUserDetails(userId);
	}
	
	@Override
	public User getUserDetails(String email) throws UserNotFoundException {
		return this.accountService.getUserDetails(email);
	}
	
	@Override
	public void removeUser(long userId) throws UserNotFoundException {
		this.accountService.removeUser(userId);
	}
	
	@Override
	public void removeUser(String email) throws UserNotFoundException {
		this.accountService.removeUser(email);
	}
	
	@Override
	public long addLot(Lot lot) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			lot.sellerId = this.accountService.getCurrentUser().id;
			return this.lotService.addLot(lot);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<Lot> searchLots(Lot template) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			return this.lotService.searchLots(template);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<Lot> getUsersLots() throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			return this.lotService.getUsersLots(this.accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void updateLot(Lot lot) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.updateLot(lot);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void bidForLot(long lotId, BigDecimal amount, boolean isPrivateBid) throws RequiresLoginException, UnauthorisedBidException, InvalidBidException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.bidForLot(lotId, amount, this.accountService.getCurrentUser().id, isPrivateBid);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public Bid getHighestBid(long lotId) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			return this.lotService.getHighestBid(lotId);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<Bid> getVisibleBids(long lotId) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			return this.lotService.getVisibleBids(lotId, this.accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void listenForLot(Lot template, Callback<Lot, Void> callback) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForLot(template, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}

	@Override
	public void subscribeToLot(long id, Callback<Lot, Void> callback) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.subscribeToLot(id, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public Lot getLotDetails(long lotId) throws RequiresLoginException {
		if(this.accountService.isLoggedIn()) {
			Lot lot = this.lotService.getLotDetails(lotId);
			lot.bids = this.lotService.getVisibleBids(lotId, this.accountService.getCurrentUser().id);
			return lot;
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
		
		LotIDCounter.initialiseInSpace(space);
		UserIDCounter.initialiseInSpace(space);
		
		PasswordHashScheme hashScheme = new MediumSecurityHashScheme();
		LotService lotService = new JavaSpaceLotService(space, transMgr);
		AccountService accountService = new JavaSpaceAccountService(space, hashScheme);
		AuctionView view = new JavaFXAuctionView();
		
		AuctionController controller = new ConcreteAuctionController(view, lotService, accountService);
		
		controller.launch();
	}

}
