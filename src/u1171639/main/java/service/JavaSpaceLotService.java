package u1171639.main.java.service;

import java.math.BigDecimal;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.lease.LeaseDeniedException;
import net.jini.core.transaction.Transaction;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.TransactionFactory;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.HighestBid;
import u1171639.main.java.utilities.LotIDCounter;

public class JavaSpaceLotService implements LotService {
	private final JavaSpace space;
	private final TransactionManager transMgr;
	
	public JavaSpaceLotService(JavaSpace space, TransactionManager transMgr) {
		this.space = space;
		this.transMgr = transMgr;
	}
	
	@Override
	public long addLot(Lot lot) {
		try {
			// Create a highest bid tracker for this lot
			HighestBid highestBid = new HighestBid();
			highestBid.bidId = HighestBid.NO_BID_ID;
			
			LotIDCounter counter = (LotIDCounter) this.space.take(new LotIDCounter(), null, 5000);
			
			lot.id = counter.id;
			highestBid.lotId = lot.id;
			counter.increment();
			
			this.space.write(counter, null, Lease.FOREVER);
			this.space.write(lot, null, Lease.FOREVER);
			this.space.write(highestBid, null, Lease.FOREVER);
			
			return lot.id;
			
		} catch (RemoteException | TransactionException | UnusableEntryException | InterruptedException e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	@Override
	public List<Lot> searchLots(Lot template) {
		List<Lot> retrievedLots = new ArrayList<Lot>();
		
		// Create a transaction
		Transaction.Created trc = null;
		
		try {
			trc = TransactionFactory.create(this.transMgr, Lease.FOREVER);
		} catch(RemoteException | LeaseDeniedException e) {
			// TODO
		}
		
		Transaction transaction = trc.transaction;
		
		boolean lotsToTake = true;
		while(lotsToTake) {
			try {
				Lot retrievedLot = (Lot) this.space.takeIfExists(template, transaction, 0);
				
				if(retrievedLot != null) {
					retrievedLots.add(retrievedLot);
				} else {
					lotsToTake = false;
					transaction.abort();
				}
			} catch (RemoteException e) {
				lotsToTake = false;
			} catch (UnusableEntryException e) {
				lotsToTake = false;
			} catch (TransactionException e) {
				lotsToTake = false;
			} catch (InterruptedException e) {
				lotsToTake = false;
			}	
		}
		
		return retrievedLots;
	}
	
	@Override
	public void updateLot(Lot lot) {
		try {
			Lot template = new Lot(lot.id);
			this.space.take(template, null, Lease.FOREVER);
			this.space.write(lot, null, Lease.FOREVER);
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void bidForLot(long lotId, BigDecimal amount, long bidderId, boolean isPrivateBid) throws UnauthorisedBidException, InvalidBidException, LotNotFoundException {
		if (amount.compareTo(BigDecimal.ZERO) > 0) {
			Lot lot = getLotDetails(lotId);
			
			if(!lot.sellerId.equals(bidderId)) {
				try {
					HighestBid template = new HighestBid(lotId);
					HighestBid highestBidPtr = (HighestBid) this.space.take(template, null, Lease.FOREVER);
					Bid highestBid = null;
					
					if(highestBidPtr.hasBid()) {
						// A bid is uniquely identified using a combination of bidId and lotId.
						Bid bidTemplate = new Bid(highestBidPtr.bidId, lotId);
						highestBid = (Bid) this.space.take(bidTemplate, null, Lease.FOREVER);
					}
						
					// If the new amount is greater than the old highest bid or there is no previous bid
					if(highestBid == null || amount.compareTo(highestBid.amount)  == 1) {
						Bid newBid = new Bid(highestBidPtr.nextBidId(), lotId, bidderId, amount, isPrivateBid);
						newBid.bidTime = new Date(System.currentTimeMillis());
						this.space.write(newBid, null, Lease.FOREVER);
					} else {
						this.space.write(highestBid, null, Lease.FOREVER);
						this.space.write(highestBidPtr, null, Lease.FOREVER);
						throw new InvalidBidException("Bid amount must exceed current highest bid.");
					}
					
					if(highestBid != null) {
						this.space.write(highestBid, null, Lease.FOREVER);
					}
					this.space.write(highestBidPtr, null, Lease.FOREVER);
				} catch(RemoteException e) {
					e.printStackTrace();
				} catch (TransactionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnusableEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				throw new UnauthorisedBidException("You are not allowed to bid on your own Lot.");
			}
		} else {
			throw new InvalidBidException("Bid amount must be greater than 0");
		}
	}
	
	@Override
	public Bid getHighestBid(long lotId) {
		try {
			HighestBid template = new HighestBid(lotId);
			HighestBid highestBidPtr = (HighestBid) this.space.read(template, null, Lease.FOREVER);
			
			if(highestBidPtr.hasBid()) {
				// A bid is uniquely identified using a combination of bidId and lotId.
				Bid bidTemplate = new Bid(highestBidPtr.bidId, lotId);
				return (Bid) this.space.read(bidTemplate, null, Lease.FOREVER);
			} else {
				return null;
			}
		} catch(Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public Lot getLotDetails(long id) throws LotNotFoundException {
		Lot template = new Lot(id);
		try {
			Lot lot = (Lot) this.space.readIfExists(template, null, 0);
			if(lot != null) {
				return lot;
			} else {
				throw new LotNotFoundException("The Lot was not found.");
			}
		} catch (UnusableEntryException | TransactionException | InterruptedException | RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	@Override
	public List<Bid> getVisibleBids(long lotId, long userId) throws LotNotFoundException {
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
			publicTemplate.privateBid = false;
			
			visibleBids.addAll(searchBids(publicTemplate));
			
			// Get the private bids that belong to this user
			Bid userPrivateTemplate = new Bid();
			userPrivateTemplate.bidderId = userId;
			userPrivateTemplate.privateBid = true;
			visibleBids.addAll(searchBids(userPrivateTemplate));
		}
		
		return visibleBids;
	}
	
	private List<Bid> searchBids(Bid template) {
		List<Bid> retrievedBids = new ArrayList<Bid>();
		
		// Create a transaction
		Transaction.Created trc = null;
		
		try {
			trc = TransactionFactory.create(this.transMgr, Lease.FOREVER);
		} catch(RemoteException | LeaseDeniedException e) {
			// TODO
		}
		
		Transaction transaction = trc.transaction;
		
		boolean bidsToTake = true;
		while(bidsToTake) {
			try {
				Bid retrievedBid = (Bid) this.space.takeIfExists(template, transaction, 0);
				
				if(retrievedBid != null) {
					retrievedBids.add(retrievedBid);
				} else {
					bidsToTake = false;
					transaction.abort();
				}
			} catch (RemoteException e) {
				bidsToTake = false;
			} catch (UnusableEntryException e) {
				bidsToTake = false;
			} catch (TransactionException e) {
				bidsToTake = false;
			} catch (InterruptedException e) {
				bidsToTake = false;
			}	
		}
		
		return retrievedBids;
	}
	
	@Override
	public List<Lot> getUsersLots(long userId) {
		Lot template = new Lot();
		template.sellerId = userId;
		return searchLots(template);
	}
	

	@Override
	public void subscribeToLot(long id, final Callback<Lot, Void> callback) {
		
		final Lot template = new Lot(id);
		
		RemoteEventListener listener = new RemoteEventListener() {
			
			@Override
			public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
				try {
					Lot lot = (Lot) JavaSpaceLotService.this.space.readIfExists(template, null, 0);
					
					if(lot != null) {
						callback.call(lot);
					}
				} catch(RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnusableEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransactionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		try {
			UnicastRemoteObject.exportObject(listener,0);
			this.space.notify(template, null, listener, Lease.FOREVER, null);
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	@Override
	public void listenForLot(final Lot template, final Callback<Lot, Void> callback) {
		RemoteEventListener listener = new RemoteEventListener() {
			
			@Override
			public void notify(RemoteEvent event) throws UnknownEventException, RemoteException {
				try {
					Lot lot = (Lot) JavaSpaceLotService.this.space.readIfExists(template, null, 0);
					if(lot != null) {
						callback.call(lot);
					}
				} catch(RemoteException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (UnusableEntryException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TransactionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		};
		
		try {
			UnicastRemoteObject.exportObject(listener,0);
			this.space.notify(template, null, listener, Lease.FOREVER, null);
			
		} catch (RemoteException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TransactionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Override
	public void removeLot(long lotId, long userId) throws UnauthorisedLotActionException, LotNotFoundException {
		Lot lot = this.getLotDetails(lotId);
		
		if(lot.sellerId.equals(userId)) {
			try {
				Lot removedLot = (Lot) this.space.takeIfExists(new Lot(lotId), null, 0);
				
				if(removedLot == null) {
					throw new LotNotFoundException("Lot not found for removal.");
				}
			} catch (RemoteException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnusableEntryException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (TransactionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} else {
			throw new UnauthorisedLotActionException("Cannot remove another user's lots.");
		}
	}
}
