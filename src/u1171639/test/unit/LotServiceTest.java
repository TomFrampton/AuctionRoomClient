package u1171639.test.unit;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.net.ConnectException;

import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.exception.InvalidBidException;
import u1171639.main.exception.UnauthorisedBidException;
import u1171639.main.model.account.User;
import u1171639.main.model.lot.Bid;
import u1171639.main.model.lot.Car;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.SpaceUtils;

public class LotServiceTest {
	JavaSpaceLotService lotService;
	
	@Before
	public void setUp() throws Exception {
		JavaSpace space = SpaceUtils.getSpace("localhost");
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		this.lotService = new JavaSpaceLotService(space);
		LotIDCounter.initialiseInSpace(space);
	}

	@After
	public void tearDown() throws Exception {
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
	public void updateLot() {
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
	public void bidForLot() {
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
		
		this.lotService.subscribeToLot(car.id, new Callback<Void, Lot>() {
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
		
		this.lotService.listenForLot(car, new Callback<Void, Lot>() {
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
