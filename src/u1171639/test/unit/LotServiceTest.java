package u1171639.test.unit;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.util.List;

import net.jini.core.entry.Entry;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.service.LotService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.LotIDCounter;
import u1171639.main.java.utilities.SpaceUtils;

public class LotServiceTest {
	JavaSpaceLotService lotService;
	JavaSpace space;
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace("localhost");
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TransactionManager transMgr = SpaceUtils.getManager("localhost");
		if(transMgr == null) {
			throw new ConnectException("Could not connect to TransactionManager");
		}
		
		this.lotService = new JavaSpaceLotService(space, transMgr);
		LotIDCounter.initialiseInSpace(space);
	}

	@After
	public void tearDown() throws Exception {
		int objectCounter = 0;
		boolean somethingToTake = true;
		
		while(somethingToTake) {
			Entry entry = this.space.takeIfExists(new Lot(), null, 0);
			
			if(entry != null) {
				objectCounter++;
			} else {
				somethingToTake = false;
			}
		}
		
		somethingToTake = true;
		while(somethingToTake) {
			Entry entry = this.space.takeIfExists(new Bid(), null, 0);
			
			if(entry != null) {
				objectCounter++;
			} else {
				somethingToTake = false;
			}
		}
		
		System.out.println(objectCounter);
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
		
		car.id = this.lotService.addLot(car);
		Car retrievedCar = (Car) this.lotService.getLotDetails(car.id);
		
		car2.id = this.lotService.addLot(car2);
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
		
		this.lotService.addLot(car1);
		this.lotService.addLot(car2);
		this.lotService.addLot(car3);
		this.lotService.addLot(car4);
		
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
	}
	
	@Test
	public void testUpdateLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "UpdateLot";
		car.sellerId = 0l;
		
		car.id = this.lotService.addLot(car);
		
		car.description = "Updated!";
		this.lotService.updateLot(car);
		
		Car retrievedCar = (Car) this.lotService.getLotDetails(car.id);
		
		assertTrue(retrievedCar.id.equals(car.id));
		assertTrue(retrievedCar.make.equals(car.make));
		assertTrue(retrievedCar.model.equals(car.model));
		assertTrue(retrievedCar.sellerId.equals(car.sellerId));
		assertTrue(retrievedCar.description.equals(car.description));
	}
	
	@Test
	public void testBidForLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "BidForLot";
		car.sellerId = 0l;
		car.id = this.lotService.addLot(car);
		
		Car car2 = new Car();
		car2.make = "UnitTest2"; 
		car2.model = "BidForLot2";
		car2.sellerId = 0l;
		car2.id = this.lotService.addLot(car2);
		
		try {
			this.lotService.bidForLot(car.id, new BigDecimal(1000.00), 0l);
			fail("User should not be able to bid on own Lot");
		} catch (UnauthorisedBidException e) {
			// Pass
		} catch (InvalidBidException e) {
			fail("Bid amount is valid.");
		}
		
		try {
			this.lotService.bidForLot(car.id, new BigDecimal(1000.00), 1l);
		} catch (UnauthorisedBidException e) {
			fail("User should be able to bid on other User's Lots.");
		} catch (InvalidBidException e) {
			fail("Bid amount is valid.");
		}
		
		try {
			this.lotService.bidForLot(car.id, new BigDecimal(500.00), 1l);
			fail("Should not be able to bid lower than currrent bid.");
		} catch (UnauthorisedBidException e) {
			fail("User should be able to bid on other User's Lots.");
		} catch (InvalidBidException e) {
			// Pass
		}
		
		try {
			this.lotService.bidForLot(car2.id, new BigDecimal(200.00), 1l);
		} catch (UnauthorisedBidException e) {
			fail("User should be able to bid on other User's Lots.");
		} catch (InvalidBidException e) {
			fail("Bid amount is valid.");
		}
		
		Bid highestBid = this.lotService.getHighestBid(car.id);
		Bid highestBid2 = this.lotService.getHighestBid(car2.id);
		
		assertTrue(highestBid.amount.compareTo(new BigDecimal(1000.0)) == 0);
		assertTrue(highestBid2.amount.compareTo(new BigDecimal(200.0)) == 0);
	}
	
	@Test
	public void testSubscribeToLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "SubscribeToLot";
		car.sellerId = 0l;
		car.id = this.lotService.addLot(car);
		
		final Object finished = new Object();
		
		// Used for returning the lot in the callback
		final Lot[] lot = new Lot[1];
		
		this.lotService.subscribeToLot(car.id, new Callback<Lot, Void>() {
			@Override
			public Void call(Lot changedLot) {
				lot[0] = changedLot;
				
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		});
		
		try {
			synchronized(finished) {
				car.description = "Updated!";
				this.lotService.updateLot(car);
				finished.wait();
			}
			
			Car retrievedCar = (Car) lot[0];
			
			assertTrue(retrievedCar.id.equals(car.id));
			assertTrue(retrievedCar.make.equals(car.make));
			assertTrue(retrievedCar.model.equals(car.model));
			assertTrue(retrievedCar.sellerId.equals(car.sellerId));
			assertTrue(retrievedCar.description.equals(car.description));
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	@Test
	public void testListenForLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "ListenForLot";
		
		final Object finished = new Object();
		
		// Used for returning the lot in the callback
		final Lot[] lot = new Lot[1];
		
		this.lotService.listenForLot(car, new Callback<Lot, Void>() {
			@Override
			public Void call(Lot addedLot) {
				lot[0] = addedLot;
				
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		});
		
		try {
			synchronized(finished) {
				car.id = this.lotService.addLot(car);
				finished.wait();
			}
			
			Car retrievedCar = (Car) lot[0];
			
			assertTrue(retrievedCar.id.equals(car.id));
			assertTrue(retrievedCar.make.equals(car.make));
			assertTrue(retrievedCar.model.equals(car.model));
		} catch(InterruptedException e) {
			e.printStackTrace();
		}
	}

}
