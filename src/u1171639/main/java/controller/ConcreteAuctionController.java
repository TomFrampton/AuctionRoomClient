package u1171639.main.java.controller;

import java.net.ConnectException;
import java.util.List;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.exception.UnauthorisedNotificationActionException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
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
	public long addLot(Lot lot, Callback<Bid, Void> bidCallback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			lot.sellerId = this.accountService.getCurrentUser().id;
			return this.lotService.addLot(lot, bidCallback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public List<Lot> searchLots(Lot template) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			return this.lotService.searchLots(template);
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
	public void updateLot(Lot lot) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.updateLot(lot);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void bidForLot(Bid bid) throws RequiresLoginException, UnauthorisedBidException, InvalidBidException, LotNotFoundException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			bid.bidderId = this.accountService.getCurrentUser().id;
			this.lotService.bidForLot(bid);
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
	public List<Bid> getVisibleBids(long lotId) throws RequiresLoginException, LotNotFoundException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			List<Bid> bids = this.lotService.getVisibleBids(lotId, this.accountService.getCurrentUser().id);
			
			for(Bid bid : bids) {
				try {
					bid.bidder = this.accountService.getUserDetails(bid.bidderId);
				} catch (UserNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
			return bids;
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void listenForLot(Lot template, Callback<Lot, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForLot(template, this.accountService.getCurrentUser().id, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}

	@Override
	public void subscribeToLotUpdates(long id, Callback<Lot, Void> callback) throws RequiresLoginException, NotificationException, LotNotFoundException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.subscribeToLotUpdates(id, this.accountService.getCurrentUser().id, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}
	
	@Override
	public void listenForBidsOnLot(long lotId, final Callback<Bid, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.lotService.listenForBidsOnLot(lotId, callback);
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
	public List<Notification> retrieveAllNotifications() throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			return this.notificationService.retrieveAllNotifications(this.accountService.getCurrentUser().id);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
	}

	@Override
	public void listenForNotifications(Callback<Notification, Void> callback) throws RequiresLoginException, AuctionCommunicationException {
		if(this.accountService.isLoggedIn()) {
			this.notificationService.listenForNotifications(this.accountService.getCurrentUser().id, callback);
		} else {
			throw new RequiresLoginException("User must be logged in to partake in auction");
		}
		
	}
	
	@Override
	public void addNotification(Notification notification) throws RequiresLoginException, AuctionCommunicationException {
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
	
	public static void main(String[] args) {
		JavaSpace space = SpaceUtils.getSpace(SpaceConsts.HOST);
		if(space == null) {
			FXApplicationStart.showError();
			System.exit(1);
		}
		
		TransactionManager transMgr = SpaceUtils.getManager(SpaceConsts.HOST);
		if(transMgr == null) {
			FXApplicationStart.showError();
			System.exit(1);
		}
		
		LotIDCounter.initialiseInSpace(space);
		UserIDCounter.initialiseInSpace(space);
		BidIDCounter.initialiseInSpace(space);
		NotificationIDCounter.initialiseInSpace(space);
		
		PasswordHashScheme hashScheme = new MediumSecurityHashScheme();
		
		LotService lotService = new JavaSpaceLotService(space, transMgr);
		AccountService accountService = new JavaSpaceAccountService(space, hashScheme);
		NotificationService notificationService = new JavaSpaceNotificationService(space, transMgr);
		
		AuctionView view = new JavaFXAuctionView();
		
		AuctionController controller = new ConcreteAuctionController(view, lotService, accountService, notificationService);
		
		controller.launch();
	}
}
