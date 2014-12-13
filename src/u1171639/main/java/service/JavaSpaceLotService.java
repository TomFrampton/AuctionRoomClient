package u1171639.main.java.service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.BidNotFoundException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.TransactionUtils;
import u1171639.main.java.utilities.counters.BidIDCounter;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.main.java.utilities.flags.BidAcceptedFlag2;
import u1171639.main.java.utilities.flags.BidPlacedFlag;
import u1171639.main.java.utilities.flags.LotAddedFlag;
import u1171639.main.java.utilities.flags.LotRemovedFlag;
import u1171639.main.java.utilities.flags.LotUpdatedFlag;

/**
 * A JavaSpace specific implementation of LotService. Provides methods for manipulating
 * lots and bids in the space.
 * @author U117169
 */
public class JavaSpaceLotService implements LotService {
	private final JavaSpace05 space;
	private final TransactionManager transMgr;
	
	/**
	 * Instantiate this lot service using the JavaSpace and TransactionManager provided.
	 * @param space The JavaSpace service proxy
	 * @param transMgr The TransactionManager service proxy
	 */
	public JavaSpaceLotService(JavaSpace space, TransactionManager transMgr) {
		this.space = (JavaSpace05) space;
		this.transMgr = transMgr;
	}
	
	/**
	 * Adds a lot to the auction and subscribes to the lot to listens for bids that are made on the lot.
	 * @param lot The lot to add.
	 * @param bidCallback The callback that will be called when a bid is made on this lot. Can be null.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 * @return The unique ID that has been assigned to this Lot.
	 */
	@Override
	public long addLot(Lot lot, final Callback<Bid, Void> bidCallback) throws AuctionCommunicationException {	
		// Create a transaction
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			// Get the lot counter so we can assign a unique ID for this Lot. Take it from the space to enforce
			// mutual exclusion when incrementing and assigning the ID number.
			LotIDCounter counter = (LotIDCounter) this.space.takeIfExists(new LotIDCounter(), transaction, SpaceConsts.WAIT_TIME);
			
			if(counter == null) {
				throw new AuctionCommunicationException();
			}
			
			lot.id = counter.id;
			counter.increment();
			
			lot.timeAdded = new Date(System.currentTimeMillis());
			
			// As we are adding a Lot we would like to be notified when someone bids on it.
			this.listenForBidsOnLot(lot.id, bidCallback);
			
			// Add the flag to announce that we have added a Lot in the space
			LotAddedFlag lotAddedFlag = new LotAddedFlag();
			lotAddedFlag.lotId = lot.id;
			
			this.space.write(lot, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			this.space.write(counter, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			
			this.space.write(lotAddedFlag, transaction, SpaceConsts.FLAG_WRITE_TIME);
			
			TransactionUtils.commit(transaction);
			
			// Something went wrong. Abort the transaction.
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
		
		return lot.id;
	}
	
	/**
	 * Search for lots that match a Lot template.
	 * @param template The lot template to search with.
	 * @return A list of lots found.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 * @return A list of lots from the auction that match the template.
	 */
	@Override
	public List<Lot> searchLots(Lot template) throws AuctionCommunicationException {
		List<Lot> retrievedLots = new ArrayList<Lot>();
		
		// Create a transaction
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			// We will use this template multiple times.
			Entry snapshot = this.space.snapshot(template);
			
			// Keep taking lots out until there are no more that match the template
			boolean lotsToTake = true;
			while(lotsToTake) {
					Lot retrievedLot = (Lot) this.space.takeIfExists(snapshot, transaction, SpaceConsts.WAIT_TIME);
					
					if(retrievedLot != null) {
						retrievedLots.add(retrievedLot);
					} else {
						// No more lots. Abort the transaction to return the lots to the space.
						lotsToTake = false;
						TransactionUtils.abort(transaction);
					}
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			// Something went wrong. Abort transaction to roll back.
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
		
		return retrievedLots;
	}
	
	/**
	 * Update the lot in the auction with the ID of the lot passed in to the lot passed in.
	 * @param lot The lot's new details.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 */
	@Override
	public void updateLot(Lot lot) throws AuctionCommunicationException {
		Lot template = new Lot(lot.id);
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			LotUpdatedFlag lotUpdatedFlag = new LotUpdatedFlag();
			lotUpdatedFlag.lotId = lot.id;
			
			space.write(lotUpdatedFlag, transaction, SpaceConsts.FLAG_WRITE_TIME);
			
			// Remove the old lot and replace with this new one
			this.space.take(template, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			this.space.write(lot, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			
			TransactionUtils.commit(transaction);
			
		} catch(RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
	}
	
	/**
	 * Place a bid for a lot in the auction.
	 * @param lotId The ID of the lot to bid for.
	 * @param amount The amount you would like to bid.
	 * @param bidderId The ID of the bidder.
	 * @param isPrivateBid Is this bid a private bid?
	 * @throws UnauthorisedBidException Thrown if you are not allowed to bid on this lot e.g. it is your lot.
	 * @throws InvalidBidException Thrown if the bid amount doesn't exceed the current visible highest bid.
	 * @throws LotNotFoundException Thrown if the lot for which a bid is being placed does not exist in the auction.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 * @return The ID of the Bid that was added.
	 */
	@Override
	public long bidForLot(Bid bid) throws UnauthorisedBidException, InvalidBidException, LotNotFoundException, AuctionCommunicationException {
		
		if (bid.amount.compareTo(BigDecimal.ZERO) < 0) {
			throw new InvalidBidException("Bid amount must be greater than 0");
		}
		
		Lot lot = getLotDetails(bid.lotId);
		
		if(lot.sellerId.equals(bid.bidderId)) {
			throw new UnauthorisedBidException("You are not allowed to bid on your own Lot.");
		}
			
		Bid highestBid = getHighestBid(bid.lotId, bid.bidderId);
		
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {			
			// If the bid being placed does not exceed the current highest bid.
			if(highestBid != null && !(bid.amount.compareTo(highestBid.amount)  == 1)) {
				throw new InvalidBidException("Bid amount must exceed current highest bid.");
			} else {
				bid.bidTime = new Date(System.currentTimeMillis());
				
				// Get the bid counter so we can assign a unique ID for this Bid. Take it from the space to enforce
				// mutual exclusion when incrementing and assigning the ID number.
				BidIDCounter counter = (BidIDCounter) this.space.takeIfExists(new BidIDCounter(), transaction, SpaceConsts.TRANSACTION_TIME);
				
				if(counter == null) {
					throw new AuctionCommunicationException();
				}
				
				bid.id = counter.id;
				counter.increment();
				
				// Set a flag in the space to announce to listeners that a new bid has been made and
				// give the information required to find the new bid.
				BidPlacedFlag bidPlacedFlag = new BidPlacedFlag();
				bidPlacedFlag.lotId = bid.lotId;
				bidPlacedFlag.bidId = bid.id;
				
				this.space.write(bid, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
				this.space.write(counter, transaction, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
				
				this.space.write(bidPlacedFlag, transaction, SpaceConsts.FLAG_WRITE_TIME);
				
				TransactionUtils.commit(transaction);
				
				return bid.id;
			}
		} catch(RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
	}
	
	/**
	 * Get the highest bid for a lot that is visible from this user's point of view.
	 * @throws LotNotFoundException The lot for which the bid was being placed does not exist in the auction.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 * @return The highest bid for this lot that is visible to the user.
	 */
	@Override
	public Bid getHighestBid(long lotId, long userId) throws LotNotFoundException, AuctionCommunicationException {
		// Initially my application kept a HighestBid object in the space for each
		// lot to keep track of the highest bid to make sure a user didn't bid less that the
		// highest bid. However, this would allow the user to 'see' a winning private bid
		// that shouldn't be visible. Therefore, this method gets the highest bid that the
		// user can see i.e. all public bids, private bids made by the user, or all bids on a user's lot.
		
		List<Bid> visibleBids = getVisibleBids(lotId, userId);
		
		// Find the bid with the highest value
		if(visibleBids.isEmpty()) {
			return null;
		} else {
			return Collections.max(visibleBids, new Comparator<Bid>() {
				@Override
				public int compare(Bid o1, Bid o2) {
					return o1.amount.compareTo(o2.amount);
				}
			});
		}
	}

	/**
	 * Get all details of a lot.
	 * @param id The id of the lot to look for.
	 * @throws LotNotFoundException The lot with this ID was not found.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 * @return The lot object.
	 */
	@Override
	public Lot getLotDetails(long id) throws LotNotFoundException, AuctionCommunicationException {
		Lot template = new Lot(id);
		
		try {
			// No need for a transaction. Using 'read' so nothing to roll back on failure
			Lot lot = (Lot) this.space.readIfExists(template, null, SpaceConsts.TRANSACTION_TIME);
			
			if(lot != null) {
				return lot;
			} else {
				throw new LotNotFoundException("The Lot was not found.");
			}
		} catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException e) {
			throw new AuctionCommunicationException();
		}
	}
	
	@Override
	public Bid getBidDetails(long id) throws AuctionCommunicationException, BidNotFoundException {
		Bid template = new Bid(id);
		
		try {
			// No need for a transaction. Using 'read' so nothing to roll back on failure
			Bid bid = (Bid) this.space.readIfExists(template, null, SpaceConsts.TRANSACTION_TIME);
			
			if(bid != null) {
				return bid;
			} else {
				throw new BidNotFoundException("The Bid was not found.");
			}
		} catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException e) {
			throw new AuctionCommunicationException();
		}
	}
	
	/**
	 * Retrieves all bids for a lot that are visible to the user.
	 * i.e. public bids or private bids made by the user or on one of the user's lots.
	 * @param lotId The ID of the lot to search for bids for.
	 * @param userId The ID of the user whose perspective we are searching from.
	 * @throws LotNotFoundException The lot for which you are searching bids for does not exist in the auction.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 * @return A list of bids visible to this user on this lot.
	 */
	@Override
	public List<Bid> getVisibleBids(long lotId, long userId) throws LotNotFoundException, AuctionCommunicationException {
		List<Bid> visibleBids = new ArrayList<Bid>();
		
		// If the lot was placed by the user then show all bids
		Lot lot = getLotDetails(lotId);
		
		if(lot.sellerId.equals(userId)) {
			Bid allTemplate = new Bid();
			allTemplate.lotId = lotId;
			
			visibleBids.addAll(searchBids(allTemplate));
		} else {
			// Get all public bids for this lot
			Bid publicTemplate = new Bid();
			publicTemplate.lotId = lotId;
			publicTemplate.privateBid = false;
			
			visibleBids.addAll(searchBids(publicTemplate));
			
			// Get the private bids that belong to this user
			Bid userPrivateTemplate = new Bid();
			userPrivateTemplate.lotId = lotId;
			userPrivateTemplate.bidderId = userId;
			userPrivateTemplate.privateBid = true;
			visibleBids.addAll(searchBids(userPrivateTemplate));
		}
		
		return visibleBids;
	}
	
	/**
	 * Accept a bid for a lot.
	 * @param bidId The ID of the bid to accept
	 * @throws BidNotFoundException If a Bid with the ID supplied does not exist.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 */
	@Override
	public void acceptBid(long bidId) throws BidNotFoundException, AuctionCommunicationException {
		// Make sure the bid exists
		Bid bid = this.getBidDetails(bidId);
		if(bid == null) {
			throw new BidNotFoundException("Bid was not found.");
		}
		
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			// Announce that this bid has been accepted
			BidAcceptedFlag2 bidAccepted = new BidAcceptedFlag2();
			bidAccepted.lotId = bid.lotId;
			bidAccepted.bidId = bid.id;
			bidAccepted.lot = this.getLotDetails(bidAccepted.lotId);
			
			this.space.write(bidAccepted, transaction, SpaceConsts.FLAG_WRITE_TIME);
			this.space.take(new Lot(bidAccepted.lotId), transaction, SpaceConsts.WAIT_TIME);
			
			TransactionUtils.commit(transaction);
			
		} catch (RemoteException | TransactionException | LotNotFoundException | UnusableEntryException | InterruptedException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
	}
	
	
	/**
	 * Get all lots that have been created by a given user.
	 * @param userId The user whose lots to search for.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 * @return A list of lots for this user.
	 */
	@Override
	public List<Lot> getUsersLots(long userId) throws AuctionCommunicationException {
		Lot template = new Lot();
		template.sellerId = userId;
		return searchLots(template);
	}
	
	/**
	 * Removes a lot from the auction.
	 * @param lotId The ID of the lot to remove.
	 * @param userId The ID of the user who requested the removal.
	 * @throws UnauthorisedLotActionException Thrown if the user does not have permission to remove this lot. i.e. it isn't their lot.
	 * @throws LotNotFoundException The lot with that ID was not found in the auction.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 */
	@Override
	public void removeLot(long lotId, long userId) throws UnauthorisedLotActionException, LotNotFoundException, AuctionCommunicationException {
		// Use transaction because we want the removal of the lot and the addition of the lotRemovedFlag to be atomic
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			// Take the lot
			Lot lot = (Lot) this.space.takeIfExists(new Lot(lotId), transaction, SpaceConsts.WAIT_TIME);
			if(lot == null) {
				TransactionUtils.abort(transaction);
				throw new LotNotFoundException("The Lot to remove was not found.");
			}
			
			// Check that the user has authority to remove this lot.
			if(!lot.sellerId.equals(userId)) {
				TransactionUtils.abort(transaction);
				throw new UnauthorisedLotActionException("Cannot remove another user's lots.");
			}
			
			// Add the flag to announce that this lot has been removed
			LotRemovedFlag lotRemovedFlag = new LotRemovedFlag();
			lotRemovedFlag.lotId = lot.id;
			lotRemovedFlag.removedLot = lot;
			
			this.space.write(lotRemovedFlag, transaction, SpaceConsts.FLAG_WRITE_TIME);
			
			TransactionUtils.commit(transaction);
			
		} catch (RemoteException | TransactionException |InterruptedException | UnusableEntryException e) {
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
	}
	
	/**
	 * Searches for and retrieves all bids that match a given template.
	 * @param template The template with which to search for bids.
	 * @return A list of bid that match the template.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 */
	private List<Bid> searchBids(Bid template) throws AuctionCommunicationException {
		List<Bid> retrievedBids = new ArrayList<Bid>();
		
		Transaction transaction = TransactionUtils.create(this.transMgr);
		
		try {
			// We will use this bid template multiple times
			Entry snapshot = this.space.snapshot(template);
			
			// Keep taking bids from the space until no more match our template
			boolean bidsToTake = true;
			while(bidsToTake) {
				Bid retrievedBid = (Bid) this.space.takeIfExists(snapshot, transaction, 0);
				
				if(retrievedBid != null) {
					retrievedBids.add(retrievedBid);
				} else {
					bidsToTake = false;
					// No more bids to find. Return bids to space.
					TransactionUtils.abort(transaction);
				}
			}
		} catch (RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
			// Something went wrong. Abort transaction to roll back.
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}	
		
		return retrievedBids;
	}
	
	
	
	/** ------------------------------------------------ NOTIFICATION METHODS ---------------------------------------------------------------------------- **/
	
	/**
	 * Subscribe to a lot to receive notifications if the lot changes.
	 * @param lotId The ID of the lot to subscribe to.
	 * @param userId The ID of the user who is subscribing.
	 * @param callback A callback that is called when the lot is updated.
	 * @throws NotificationException Thrown if you are already subscribed to this lot.
	 * @throws LotNotFoundException If the lot you are subscribing to is not found.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 */
	@Override
	public void listenForLotUpdates(final long lotId, final Callback<Lot, Void> callback) throws LotNotFoundException, AuctionCommunicationException {
		
		Transaction transaction = TransactionUtils.create(this.transMgr);
		try {
			// Make sure the lot exists.
			Lot lot = (Lot) this.space.readIfExists(new Lot(lotId), transaction, SpaceConsts.WAIT_TIME);
			if(lot == null) {
				// The lot doesn't exist in the space. Cancel the transaction.
				TransactionUtils.abort(transaction);
				throw new LotNotFoundException("That Lot was not found.");
			}
		
			// Create a  template to listen for an 'Updated' flag for this Lot. No need to use registerForAvailabiltyEvent
			// because we don't need to get the LotUpdatedFlag to be able to do ahead and get the Lot. (We already know the Lot ID).
			LotUpdatedFlag flagTemplate = new LotUpdatedFlag();
			flagTemplate.lotId = lot.id;
			
			// Create the listener
			RemoteEventListener listener = new RemoteEventListener() {
				@Override
				public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
					try {
						// We know that the Lot has been updated. Get the updated Lot.
						Lot lot = (Lot) space.readIfExists(new Lot(lotId), null, SpaceConsts.WAIT_TIME);
						
						// If the lot was found execute the callback.
						if(lot != null) {
							callback.call(lot);
						}
					} catch(RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
						// Don't execute the callback
					}
				}
			};
			
			UnicastRemoteObject.exportObject(listener, 0);
			// Tell the space to notify us of changes to this lot
			this.space.notify(flagTemplate, null, listener, SpaceConsts.AUCTION_ENTITY_WRITE_TIME, null);
			
			TransactionUtils.commit(transaction);
			
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			// Something went wrong. Roll back everything.
			TransactionUtils.abort(transaction);
			throw new AuctionCommunicationException();
		}
	}
	
	/**
	 * Listen to the auction to see if a lot that matches the specified template is added and
	 * notify us if that happens.
	 * @param template The template that matches lots that we are interested in.
	 * @param userId The ID of the user who is listening.
	 * @param callback The callback that will be called when such a lot is entered in the space.
	 * @throws AuctionCommunicationException There was an error when communicating with the auction server.
	 */
	@Override
	public void listenForLotAddition(final Lot template, final Callback<Lot, Void> callback) throws AuctionCommunicationException {
		// registerForAvailabilityEvent requires a list of templates
		List<LotAddedFlag> templates = new ArrayList<LotAddedFlag>();
		LotAddedFlag flagTemplate = new LotAddedFlag();
		
		templates.add(flagTemplate);
				
		RemoteEventListener listener = new RemoteEventListener() {
			@Override
			public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
				AvailabilityEvent event = (AvailabilityEvent) theEvent;
				
				try {
					// We know that a Lot has been added. See if the Lot that was added matches our template.
					LotAddedFlag addedLotFlag = (LotAddedFlag) event.getEntry();
					template.id = addedLotFlag.lotId;
					
					// Try to get the Lot that was added using our template + the ID of the Lot that was added.
					Lot lot = (Lot) space.readIfExists(template, null, SpaceConsts.WAIT_TIME);
					
					// Lot was found.
					if(lot != null) {
						callback.call(lot);
					}
					
				} catch(RemoteException | UnusableEntryException | TransactionException | InterruptedException e) {
					// Lot not found. Don't call the callback.
				}
			}
		};
		
		try {
			// Export the listener and ask the space to notify us when a lot matching the template is added.
			UnicastRemoteObject.exportObject(listener, 0);
			this.space.registerForAvailabilityEvent(templates, null, false, listener, SpaceConsts.AUCTION_ENTITY_WRITE_TIME, null);
			
		} catch (RemoteException | TransactionException e) {
			throw new AuctionCommunicationException();
		}
	}
	
	@Override
	public void listenForLotRemoval(long lotId, final Callback<Lot, Void> callback) throws AuctionCommunicationException {
		// registerForAvailabilityEvent requires a list of templates
		List<LotRemovedFlag> templates = new ArrayList<LotRemovedFlag>();
		LotRemovedFlag template = new LotRemovedFlag();
		template.lotId = lotId;
		templates.add(template);
				
		RemoteEventListener listener = new RemoteEventListener() {
			@Override
			public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
				AvailabilityEvent event = (AvailabilityEvent) theEvent;
				
				try {
					// Retrieve the entry that triggered the notification
					LotRemovedFlag lotRemovedFlag = (LotRemovedFlag) event.getEntry();
					
					if(callback != null) {
						callback.call(lotRemovedFlag.removedLot);
					}
					
				} catch (UnusableEntryException e) {
					// An error occurred. Do not call the callback.
				}
			}
		};
		
		try {
			UnicastRemoteObject.exportObject(listener, 0);
			space.registerForAvailabilityEvent(templates, null, true, listener, SpaceConsts.AUCTION_ENTITY_WRITE_TIME, null);
		} catch(RemoteException | TransactionException e) {
			throw new AuctionCommunicationException();
		}
	}
	
	@Override
	public void listenForBidsOnLot(long lotId, final Callback<Bid, Void> callback) throws AuctionCommunicationException {
		// registerForAvailabilityEvent requires a list of templates
		List<BidPlacedFlag> templates = new ArrayList<BidPlacedFlag>();
		BidPlacedFlag template = new BidPlacedFlag();
		template.lotId = lotId;
		
		templates.add(template);
				
		// Set up our listener to call our callback when notified
		RemoteEventListener listener = new RemoteEventListener() {
			@Override
			public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
				AvailabilityEvent event = (AvailabilityEvent) theEvent;
				
				try {
					// Retrieve the entry that triggered the notification
					BidPlacedFlag bidPlacedFlag = (BidPlacedFlag) event.getEntry();
					
					// Get the details of the bid that was just placed
					Bid bid = getBidDetails(bidPlacedFlag.bidId);
					
					if(callback != null) {
						bid.lot = getLotDetails(bid.lotId);
						callback.call(bid);
					}
					
				} catch (UnusableEntryException | LotNotFoundException | AuctionCommunicationException | BidNotFoundException e) {
					// An error occurred. Do not call the callback.
				}
			}
		};
		
		try {
			UnicastRemoteObject.exportObject(listener, 0);
			space.registerForAvailabilityEvent(templates, null, true, listener, SpaceConsts.AUCTION_ENTITY_WRITE_TIME, null);
		} catch(RemoteException | TransactionException e) {
			throw new AuctionCommunicationException();
		}
	}
	
	@Override
	public void listenForAcceptedBidOnLot(long lotId, final Callback<Bid, Void> callback) throws AuctionCommunicationException {
		// registerForAvailabilityEvent requires a list of templates
		List<BidAcceptedFlag2> templates = new ArrayList<BidAcceptedFlag2>();
		BidAcceptedFlag2 template = new BidAcceptedFlag2();
		template.lotId = lotId;
		
		templates.add(template);
				
		// Set up our listener to call our callback when notified
		RemoteEventListener listener = new RemoteEventListener() {
			@Override
			public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
				AvailabilityEvent event = (AvailabilityEvent) theEvent;
				
				try {
					// Retrieve the entry that triggered the notification
					BidAcceptedFlag2 bidAcceptedFlag = (BidAcceptedFlag2) event.getEntry();
					
					// Get the details of the bid that was accepted
					Bid bid = getBidDetails(bidAcceptedFlag.bidId);
					
					if(callback != null) {
						bid.lot = bidAcceptedFlag.lot;
						callback.call(bid);
					}
					
				} catch (UnusableEntryException | AuctionCommunicationException | BidNotFoundException e) {
					// An error occurred. Do not call the callback.
				}
			}
		};
		
		try {
			UnicastRemoteObject.exportObject(listener, 0);
			space.registerForAvailabilityEvent(templates, null, true, listener, SpaceConsts.AUCTION_ENTITY_WRITE_TIME, null);
		} catch(RemoteException | TransactionException e) {
			throw new AuctionCommunicationException();
		}
	}
}
