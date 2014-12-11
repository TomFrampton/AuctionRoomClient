package u1171639.test.unit;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.BidNotFoundException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.counters.BidIDCounter;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.test.utilities.TestUtils;

public class LotServiceTest {
	private JavaSpaceLotService lotService;
	private JavaSpace space;
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace(SpaceConsts.HOST);
		if(this.space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TransactionManager transMgr = SpaceUtils.getManager(SpaceConsts.HOST);
		if(transMgr == null) {
			throw new ConnectException("Could not connect to TransactionManager");
		}
		
		LotIDCounter.initialiseInSpace(this.space);
		BidIDCounter.initialiseInSpace(this.space);
		
		this.lotService = new JavaSpaceLotService(this.space, transMgr);
	}

	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new Bid(), this.space);
		TestUtils.removeAllFromSpace(new Lot(), this.space);
		TestUtils.removeAllFromSpace(new LotIDCounter(), this.space);
		TestUtils.removeAllFromSpace(new BidIDCounter(), this.space);
	}

	@Test
	public void testAddLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "AddLot";
		car.sellerId = 0l;
		
		Car car2 = new Car();
		car2.make = "UnitTest2"; 
		car2.model = "AddLot2";
		car2.sellerId = 1l;
		
		try {
			// Just add the lots. Don't test the bid callback just yet.
			car.id = this.lotService.addLot(car, null);
			Car retrievedCar = (Car) this.lotService.getLotDetails(car.id);
			
			car2.id = this.lotService.addLot(car2, null);
			Car retrievedCar2 = (Car) this.lotService.getLotDetails(car2.id);
			
			assertTrue(retrievedCar.id.equals(car.id));
			assertTrue(retrievedCar.make.equals(car.make));
			assertTrue(retrievedCar.model.equals(car.model));
			assertTrue(retrievedCar.sellerId.equals(car.sellerId));
			
			assertTrue(retrievedCar2.id.equals(retrievedCar.id + 1));
			assertTrue(retrievedCar2.id.equals(car2.id));
			assertTrue(retrievedCar2.make.equals(car2.make));
			assertTrue(retrievedCar2.model.equals(car2.model));
			assertTrue(retrievedCar2.sellerId.equals(car2.sellerId));
		} catch(LotNotFoundException e) {
			fail("Lots exist. Should have been found");
		} catch (AuctionCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testSearchLots() {
		Car car1 = new Car();
		car1.make = "Honda"; 
		car1.model = "Civic";
		car1.description = "A really nice car!";
		car1.sellerId = 0l;
		
		Car car2 = new Car();
		car2.make = "Honda"; 
		car2.model = "NSX";
		car2.description = "A really nice car!";
		car2.sellerId = 0l;
		
		Car car3 = new Car();
		car3.make = "Ford"; 
		car3.model = "Focus";
		car3.description = "A really rubbish car!";
		car3.sellerId = 0l;
		
		Car car4 = new Car();
		car4.make = "Ford"; 
		car4.model = "Fiesta";
		car4.description = "A really nice car!";
		car4.sellerId = 1l;
		
		try {
			this.lotService.addLot(car1, null);
			this.lotService.addLot(car2, null);
			this.lotService.addLot(car3, null);
			this.lotService.addLot(car4, null);
		} catch (AuctionCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		try {
			// Search 1
			Car template1 = new Car();
			template1.make = "Honda";
			
			List<Lot> search1 = this.lotService.searchLots(template1);
			
			assertTrue(search1.size() == 2);
			
			for(Lot lot : search1) {
				Car car = (Car) lot;
				assertEquals(car.make, template1.make);
			}
			
			// Search 2
			Car template2 = new Car();
			template2.make = "Ford";
			
			List<Lot> search2 = this.lotService.searchLots(template2);
			
			assertTrue(search2.size() == 2);
			
			for(Lot lot : search2) {
				Car car = (Car) lot;
				assertEquals(car.make, template2.make);
			}
			
			// Search 3
			Car template3 = new Car();
			template3.sellerId = 0l;
			
			List<Lot> search3 = this.lotService.searchLots(template3);
			
			assertTrue(search3.size() == 3);
			
			int fords = 0;
			int hondas = 0;
			
			for(Lot lot : search3) {
				Car car = (Car) lot;
				assertTrue(car.sellerId.equals(0l));
				
				if(car.make.equals("Honda")) {
					hondas++;
				} else if(car.make.equals("Ford")) {
					fords++;
				}
			}
			
			assertTrue(fords == 1);
			assertTrue(hondas == 2);
			
			// Search 4
			Car template4 = new Car();
			template4.description = "A really nice car!";
			
			List<Lot> search4 = this.lotService.searchLots(template4);
			
			assertTrue(search4.size() == 3);
			
			// Search 5
			Car template5 = new Car();
			List<Lot> search5 = this.lotService.searchLots(template5);
			
			assertTrue(search5.size() == 4);
		} catch(AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testGetUsersLots() {
		// TODO
	}
	
	@Test
	public void testUpdateLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "UpdateLot";
		car.sellerId = 0l;
		
		try {
			car.id = this.lotService.addLot(car, null);
			
			car.description = "Updated!";
			this.lotService.updateLot(car);
			
			try {
				Car retrievedCar = (Car) this.lotService.getLotDetails(car.id);
				
				assertTrue(retrievedCar.id.equals(car.id));
				assertTrue(retrievedCar.make.equals(car.make));
				assertTrue(retrievedCar.model.equals(car.model));
				assertTrue(retrievedCar.sellerId.equals(car.sellerId));
				assertTrue(retrievedCar.description.equals(car.description));
			} catch(LotNotFoundException e) {
				fail("Lots exist. Should have been found");
			}
		} catch(AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testBidForLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "BidForLot";
		car.sellerId = 0l;
		
		try {
			car.id = this.lotService.addLot(car, null);
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		Car car2 = new Car();
		car2.make = "UnitTest2"; 
		car2.model = "BidForLot2";
		car2.sellerId = 0l;
		try {
			car2.id = this.lotService.addLot(car2, null);
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		Bid bid1 = new Bid();
		bid1.lotId = car.id;
		bid1.amount = new BigDecimal(1000.00);
		bid1.bidderId = 0l;
		bid1.privateBid = false;
		
		try {
			this.lotService.bidForLot(bid1);
			fail("User should not be able to bid on own Lot");
		} catch (UnauthorisedBidException e) {
			// Pass
		} catch (InvalidBidException e) {
			fail("Bid amount is valid.");
		} catch (LotNotFoundException e) {
			fail("Lot exists. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		Bid bid2 = new Bid();
		bid2.lotId = car.id;
		bid2.amount = new BigDecimal(1000.00);
		bid2.bidderId = 1l;
		bid2.privateBid = false;
		
		try {
			this.lotService.bidForLot(bid2);
		} catch (UnauthorisedBidException e) {
			fail("User should be able to bid on other User's Lots.");
		} catch (InvalidBidException e) {
			fail("Bid amount is valid.");
		} catch (LotNotFoundException e) {
			fail("Lot exists. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		Bid bid3 = new Bid();
		bid3.lotId = car.id;
		bid3.amount = new BigDecimal(500.00);
		bid3.bidderId = 1l;
		bid3.privateBid = false;
		
		try {
			this.lotService.bidForLot(bid3);
			fail("Should not be able to bid lower than currrent bid.");
		} catch (UnauthorisedBidException e) {
			fail("User should be able to bid on other User's Lots.");
		} catch (InvalidBidException e) {
			// Pass
		} catch (LotNotFoundException e) {
			fail("Lot exists. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		Bid bid4 = new Bid();
		bid4.lotId = car2.id;
		bid4.amount = new BigDecimal(200.00);
		bid4.bidderId = 1l;
		bid4.privateBid = false;
		
		try {
			this.lotService.bidForLot(bid4);
		} catch (UnauthorisedBidException e) {
			fail("User should be able to bid on other User's Lots.");
		} catch (InvalidBidException e) {
			fail("Bid amount is valid.");
		} catch (LotNotFoundException e) {
			fail("Lot exists. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			Bid highestBid = this.lotService.getHighestBid(car.id, 1l);
			Bid highestBid2 = this.lotService.getHighestBid(car2.id, 1l);
			
			assertTrue(highestBid.amount.compareTo(new BigDecimal(1000.0)) == 0);
			assertTrue(highestBid.bidderId.equals(1l));
			
			assertTrue(highestBid2.amount.compareTo(new BigDecimal(200.0)) == 0);
			assertTrue(highestBid2.bidderId.equals(1l));
		} catch(AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (LotNotFoundException e) {
			fail("Lot was added. Should have been found");
		}
		
		try {
			// Test the bid callback functionality
			Car car3 = new Car();
			car3.make = "UnitTest3"; 
			car3.model = "BidForLot3";
			car3.sellerId = 3l;
			
			final Object lock = new Object();
			final Bid[] bid = new Bid[1];
			
			car3.id = this.lotService.addLot(car3, new Callback<Bid, Void>() {
				@Override
				public Void call(Bid param) {
					bid[0] = param;
					
					synchronized (lock) {
						lock.notify();
					}
					
					return null;
				}
			});
			
			Bid bid5 = new Bid();
			bid5.lotId = car3.id;
			bid5.amount = new BigDecimal(1000.00);
			bid5.bidderId = 1l;
			bid5.privateBid = false;
			
			this.lotService.bidForLot(bid5);
			
			synchronized (lock) {
				lock.wait();
			}
			
			assertNotNull(bid[0]);
			
		} catch(AuctionCommunicationException e) {
			fail(e.getMessage());
		} catch (UnauthorisedBidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidBidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LotNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@Test
	public void testGetVisibleBids() {
		Car car = new Car();
		car.make = "UnitTest";
		car.model = "GetVisibleLots";
		car.sellerId = 0l;
		try {
			car.id = this.lotService.addLot(car, null);
		} catch (AuctionCommunicationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		try {
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(100.00), 1l, false));
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(200.00), 1l, true));
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(300.00), 1l, true));
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(150.00), 2l, false));
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(500.00), 1l, false));
		} catch (UnauthorisedBidException | InvalidBidException e) {
			fail("Bid was valid. Exception should not have been thrown.");
		} catch (LotNotFoundException e) {
			fail("Lot exists. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(600.00), 2l, true));
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(700.00), 2l, false));
			this.lotService.bidForLot(new Bid(car.id, new BigDecimal(800.00), 2l, false));
		} catch (UnauthorisedBidException | InvalidBidException e) {
			fail("Bid was valid. Exception should not have been thrown.");
		} catch (LotNotFoundException e) {
			fail("Lot exists. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			List<Bid> visibleBids1 = this.lotService.getVisibleBids(car.id, 1l);
			Collections.sort(visibleBids1, new Comparator<Bid>() {
				@Override
				public int compare(Bid o1, Bid o2) {
					return Long.compare(o1.id, o2.id);
				}
			});
			
			assertTrue(visibleBids1.size() == 7);
			
			List<Bid> visibleBids2 = this.lotService.getVisibleBids(car.id, 2l);
			Collections.sort(visibleBids2, new Comparator<Bid>() {
				@Override
				public int compare(Bid o1, Bid o2) {
					return Long.compare(o1.id, o2.id);
				}
			});
			
			assertTrue(visibleBids2.size() == 6);
			
			List<Bid> visibleBids3 = this.lotService.getVisibleBids(car.id, 0l);
			assertTrue(visibleBids3.size() == 8);
		} catch(LotNotFoundException e) {
			fail("Lot exists. Should have been found");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}	
	}
	
	@Test
	public void testRemoveLot() {
		//TODO
	}
	
	@Test
	public void testListenForLotUpdates() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "SubscribeToLot";
		car.sellerId = 0l;
		try {
			car.id = this.lotService.addLot(car, null);
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		final Object finished = new Object();
		
		// Used for returning the lot in the callback
		final Lot[] lot = new Lot[1];
		
		try {
			this.lotService.listenForLotUpdates(car.id, new Callback<Lot, Void>() {
				@Override
				public Void call(Lot changedLot) {
					lot[0] = changedLot;
					
					synchronized(finished) {
						finished.notify();
					}
					return null;
				}
			});
		} catch (LotNotFoundException e) {
			fail("Lot was added. Should have been found.");
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			this.lotService.updateLot(car);
			waitForNotification(finished);
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
	}
	
	@Test
	public void testListenForLotAddition() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "ListenForLot";
		
		final Object finished = new Object();
		
		// Used for returning the lot in the callback
		final Lot[] lot = new Lot[1];
		
		try {
			this.lotService.listenForLotAddition(car, new Callback<Lot, Void>() {
				@Override
				public Void call(Lot addedLot) {
					lot[0] = addedLot;
					
					synchronized(finished) {
						finished.notify();
					}
					return null;
				}
			});
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
		
		try {
			synchronized(finished) {
				car.id = this.lotService.addLot(car, null);
				finished.wait();
			}
			
			Car retrievedCar = (Car) lot[0];
			
			assertTrue(retrievedCar.id.equals(car.id));
			assertTrue(retrievedCar.make.equals(car.make));
			assertTrue(retrievedCar.model.equals(car.model));
		} catch(InterruptedException e) {
			e.printStackTrace();
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testListenForLotRemoval() {
		Lot lot = new Lot();
		lot.name = "UnitTest";
		lot.description = "TestListenForLotRemoval";
		lot.sellerId = 0l;
		
		final Object lock = new Object();
		final Lot[] lotHolder = new Lot[1];
		
		try {
			lot.id = this.lotService.addLot(lot, null);
			
			this.lotService.listenForLotRemoval(lot.id, new Callback<Lot, Void>() {

				@Override
				public Void call(Lot removedLot) {
					lotHolder[0] = removedLot;
					
					synchronized (lock) {
						lock.notify();
					}
					
					return null;
				}
			});
			
			this.lotService.removeLot(lot.id, 0l);
			this.waitForNotification(lock);
			
			Lot removedLot = lotHolder[0];
			
			assertTrue(removedLot.id.equals(lot.id));
			
		} catch (AuctionCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnauthorisedLotActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LotNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListenForAcceptedBidOnLot() {
		Lot lot = new Lot();
		lot.name = "UnitTest";
		lot.description = "ListenForAcceptedBid";
		lot.sellerId = 0l;
		
		final Object lock = new Object();
		final Bid[] bid = new Bid[1];
		
		try {
			lot.id = this.lotService.addLot(lot, null);			
			long bidId = this.lotService.bidForLot(new Bid(lot.id, new BigDecimal("1000.00"), 1l, false));	
			
			this.lotService.listenForAcceptedBidOnLot(lot.id, new Callback<Bid,Void>() {
				@Override
				public Void call(Bid acceptedBid) {
					bid[0] = acceptedBid;
					
					synchronized (lock) {
						lock.notify();
					}
					
					return null;
				}
			});
			
			this.lotService.acceptBid(bidId);
			this.waitForNotification(lock);
			
			assertNotNull(bid[0]);
			assertTrue(bid[0].id.equals(bidId));
			assertTrue(bid[0].amount.equals(new BigDecimal("1000.00")));
			
		} catch (AuctionCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnauthorisedBidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidBidException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LotNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (BidNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void waitForNotification(Object lock) {
		try {
			synchronized(lock) {
				lock.wait();
			}
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
}
