package u1171639.main.java.controller;

import java.util.List;

import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import net.sf.oval.ConstraintViolation;
import net.sf.oval.Validator;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.BidNotFoundException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.exception.UnauthorisedNotificationActionException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.account.UserAccount;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.UserNotification;
import u1171639.main.java.service.AccountService;
import u1171639.main.java.service.JavaSpaceAccountService;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.service.JavaSpaceNotificationService;
import u1171639.main.java.service.LotService;
import u1171639.main.java.service.NotificationService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.MediumSecurityHashScheme;
import u1171639.main.java.utilities.PasswordHashScheme;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.counters.BidIDCounter;
import u1171639.main.java.utilities.counters.IDCounter;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.main.java.utilities.counters.NotificationIDCounter;
import u1171639.main.java.utilities.counters.UserIDCounter;
import u1171639.main.java.view.AuctionView;
import u1171639.main.java.view.FXApplicationStart;
import u1171639.main.java.view.JavaFXAuctionView;

public class ConcreteAuctionController implements AuctionController {
	private AuctionView view;
	
	private LotService lotService;
	private AccountService accountService;
	private NotificationService notificationService;
	
	public ConcreteAuctionController(AuctionView view, LotService lotService, AccountService accountService, NotificationService notificationService) {
		this.view = view;
		this.lotService = lotService;
		this.accountService = accountService;
		this.notificationService = notificationService;
	}
	
	@Override
	public void launch() {
		this.view.start(this);
	}
	
	@Override
	public long register(UserAccount newUser) throws RegistrationException, ValidationException, AuctionCommunicationException {
		validate(newUser);
		return this.accountService.register(newUser);
	}
	
	@Override
	public void login(UserAccount credentials) throws AuthenticationException, ValidationException, AuctionCommunicationException {
		credentials.forename = " ";
		credentials.surname = " ";
		
		validate(credentials);
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
	public UserAccount getCurrentUser() {
		return this.accountService.getCurrentUser();
	}
	
	@Override
	public UserAccount getUserDetails(long userId) throws UserNotFoundException, AuctionCommunicationException {
		return this.accountService.getUserDetails(userId);
	}
	
	@Override
	public UserAccount getUserDetails(String username) throws UserNotFoundException, AuctionCommunicationException {
		return this.accountService.getUserDetails(username);
	}
	
	@Override
	public void removeUser(long userId) throws UserNotFoundException, AuctionCommunicationException {
		this.accountService.removeUser(userId);
	}
	
	@Override
	public void removeUser(String username) throws UserNotFoundException, AuctionCommunicationException {
		this.accountService.removeUser(username);
	}
	
	@Override
	public long addLot(Lot lot, final Callback<Bid, Void> bidCallback) throws RequiresLoginException, AuctionCommunicationException, ValidationException {
		if(this.accountService.isLoggedIn()) {
			validate(lot);
			lot.sellerId = this.accountService.getCurrentUser().id;
			return this.lotService.addLot(lot, new Callback<Bid, Void>() {

				@Override
				public Void call(Bid bid) {
					try {
						bid.bidder = ConcreteAuctionController.this.accountService.getUserDetails(bid.bidderId);
						
						if(bidCallback != null) {
							bidCallback.call(bid);
						}
					} catch (UserNotFoundException | AuctionCommunicationException e) {
						// Something went wrong. Don't call the callback.
					}	
					
					return null;
				}
			});
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<Lot> searchLots(Lot template) throws RequiresLoginException, AuctionCommunicationException, UserNotFoundException {
		if(this.accountService.isLoggedIn()) {
			List<Lot> lots = this.lotService.searchLots(template);
			for(Lot lot : lots) {
				lot.seller = this.accountService.getUserDetails(lot.sellerId);
			}
			return lots;
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<Lot> getUsersLots() throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			return this.lotService.getUsersLots(this.accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void updateLot(Lot lot) throws RequiresLoginException, AuctionCommunicationException, ValidationException {
		if(this.accountService.isLoggedIn()) {
			validate(lot);
			this.lotService.updateLot(lot);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void bidForLot(Bid bid) throws RequiresLoginException, UnauthorisedBidException, InvalidBidException, LotNotFoundException, AuctionCommunicationException, ValidationException {
		if(this.accountService.isLoggedIn()) {
			validate(bid);
			bid.bidderId = this.accountService.getCurrentUser().id;
			this.lotService.bidForLot(bid);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void acceptBid(long bidId) throws RequiresLoginException, BidNotFoundException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.acceptBid(bidId);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public Bid getHighestBid(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			return this.lotService.getHighestBid(lotId, this.accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<Bid> getVisibleBids(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException, UserNotFoundException {
		if(this.accountService.isLoggedIn()) {
			List<Bid> bids = this.lotService.getVisibleBids(lotId, this.accountService.getCurrentUser().id);
			
			for(Bid bid : bids) {
				try {
					bid.bidder = this.accountService.getUserDetails(bid.bidderId);
				} catch (UserNotFoundException e) {
					throw new UserNotFoundException("Bidder not found.");
				}
			}
			
			return bids;
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void listenForLotAddition(Lot template, Callback<Lot, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForLotAddition(template, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}

	@Override
	public void listenForLotUpdates(long id, Callback<Lot, Void> callback) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForLotUpdates(id, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void listenForLotRemoval(long lotId, Callback<Lot, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForLotRemoval(lotId, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void listenForBidsOnLot(long lotId, final Callback<Bid, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForBidsOnLot(lotId, new Callback<Bid, Void>() {

				@Override
				public Void call(Bid bid) {
					try {
						bid.bidder = ConcreteAuctionController.this.accountService.getUserDetails(bid.bidderId);
						
						if(callback != null) {
							callback.call(bid);
						}
					} catch (UserNotFoundException | AuctionCommunicationException e) {
						// Something went wrong. Don't call the callback.
					}	
					
					return null;
				}
			});
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void listenForAcceptedBidOnLot(long lotId, final Callback<Bid, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForAcceptedBidOnLot(lotId, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public Lot getLotDetails(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			Lot lot = this.lotService.getLotDetails(lotId);
			lot.bids = this.lotService.getVisibleBids(lotId, this.accountService.getCurrentUser().id);
			return lot;
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void removeLot(long lotId) throws UnauthorisedLotActionException, LotNotFoundException, RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.removeLot(lotId, this.accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<UserNotification> retrieveAllNotifications() throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			return this.notificationService.retrieveAllNotifications(this.accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}

	@Override
	public void listenForNotifications(Callback<UserNotification, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.notificationService.listenForNotifications(this.accountService.getCurrentUser().id, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
		
	}
	
	@Override
	public void addNotification(UserNotification notification) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			notification.recipientId = this.accountService.getCurrentUser().id;
			this.notificationService.addNotification(notification);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
		
	}
	
	@Override
	public void markNotificationRead(long notificationId) throws RequiresLoginException, UnauthorisedNotificationActionException, AuctionCommunicationException, NotificationNotFoundException {
		if(this.accountService.isLoggedIn()) {
			this.notificationService.markNotificationRead(notificationId);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	private void validate(Object o) throws ValidationException {
		Validator validator = new Validator();
		List<ConstraintViolation> violations = validator.validate(o);
		
		if(violations.size() > 0) {
			throw new ValidationException(violations.size() + " validation errors.", violations);
		}
	}
	
	public static void main(String[] args) {
		String hostname = "";
		
		if(args.length > 0) {
			hostname = args[0];
		} else {
			hostname = SpaceConsts.HOST;
		}
		
		JavaSpace space = SpaceUtils.getSpace(hostname);
		if(space == null) {
			FXApplicationStart.connectionError();
			System.exit(1);
		}
		
		TransactionManager transMgr = SpaceUtils.getManager(hostname);
		if(transMgr == null) {
			FXApplicationStart.connectionError();
			System.exit(1);
		}
		
		IDCounter.initialiseInSpace(LotIDCounter.class, space);
		IDCounter.initialiseInSpace(UserIDCounter.class, space);
		IDCounter.initialiseInSpace(BidIDCounter.class, space);
		IDCounter.initialiseInSpace(NotificationIDCounter.class, space);
		
		PasswordHashScheme hashScheme = new MediumSecurityHashScheme();
		
		LotService lotService = new JavaSpaceLotService(space, transMgr);
		AccountService accountService = new JavaSpaceAccountService(space, hashScheme, transMgr);
		NotificationService notificationService = new JavaSpaceNotificationService(space, transMgr);
		
		AuctionView view = new JavaFXAuctionView();
		
		AuctionController controller = new ConcreteAuctionController(view, lotService, accountService, notificationService);
		
		controller.launch();
	}
}
