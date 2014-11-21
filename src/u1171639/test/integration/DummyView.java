package u1171639.test.integration;

import static org.junit.Assert.*;

import java.net.ConnectException;

import java.rmi.RemoteException;
import java.util.UUID;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.controller.AuctionController;
import u1171639.main.exception.AuthorisationException;
import u1171639.main.model.DistributedObject;
import u1171639.main.model.lot.Car;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.AccountService;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.HighestBid;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.SpaceUtils;
import u1171639.main.view.AuctionView;
import u1171639.test.mock.MockAccountService;

public class DummyView implements AuctionView {

	private AuctionController controller;
	private Object object;
	private JavaSpace space;
	private UUID executionId = DistributedObject.EXECUTION_ID;
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace();
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		LotService lotService = new JavaSpaceLotService(space);
		AccountService accountService = new MockAccountService();
		
		new AuctionController(this, lotService, accountService);
		
		LotIDCounter.initialiseInSpace(space);
	}
	
	

	@After
	public void tearDown() throws Exception {
		Lot template = new Lot();
		
		for(;;) {
			if(this.space.takeIfExists(template, null, 0) == null) {
				System.out.println("EXECUTIONID");
				break;
			}
		}
	}
	
	@Test
	public void registerTest() {
		
	}

	@Test
	public void testAddLot() throws Exception {
		Car car = new Car();
		car.make = "Ford";
		car.model = "Focus";
		
		long carId = this.controller.addLot(car);
		assertTrue(carId >= 0);
		
		long carId2 = this.controller.addLot(car);
		assertTrue(carId2 == carId + 1);
		
		HighestBid template1 = new HighestBid(carId);
		HighestBid template2 = new HighestBid(carId2);
		
		HighestBid carBid1 = (HighestBid) space.readIfExists(template1, null, 0);
		HighestBid carBid2 = (HighestBid) space.readIfExists(template2, null, 0);
		
		assertNotNull(carBid1);
		assertNotNull(carBid2);
		assertEquals(carBid1.bidId, HighestBid.NO_BID_ID);
		assertEquals(carBid2.bidId, HighestBid.NO_BID_ID);	
	}
	
	
	
	public void testBidForLot() throws AuthorisationException {
		Car car = new Car();
		car.make = "Test";
		car.model = "BidForLot";
		
		Car car2 = new Car();
		car.make = "Test2";
		car.model = "BidForLot2";
		
		Car car3 = new Car();
		car.make = "Test3";
		car.model = "BidForLot3";
		
		car.id = this.controller.addLot(car);
		car2.id = this.controller.addLot(car);
		car3.id = this.controller.addLot(car);
		
		assertTrue(car2.id == car.id + 1);
		assertTrue(car3.id == car2.id + 1);
		
	}
	
	public void testGetHighestBid() {
		
	}
	
	public void  testUpdateLot() {
		
	}
	
	
	@Test
	public void testSubscribeToLot() throws Exception {
		Car car = new Car();
		car.make = "Mazda";
		car.model = "RX8";
		
		car.id = this.controller.addLot(car);
		
		final Object finished = new Object();
		
		this.controller.subscribeToLot(car.id, new Callback<Void, Lot>() {
			
			@Override
			public Void call(Lot lot) {
				object = lot;
				
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		});
		
		this.controller.updateLot(car);
		
		synchronized(finished) {
			finished.wait();
		}
		
		Car retrievedCar = (Car) this.object;
		
		assertNotSame(car, retrievedCar);
		assertEquals(car.make, retrievedCar.make);
		assertEquals(car.model, retrievedCar.model);
		
		this.space.take(car, null, Lease.FOREVER);
	}
	
	
	
	@Test
	public void testListenForLot() throws Exception {
		Car car = new Car();
		car.make = "Honda";
		car.model = "Civic";
		
		final Object finished = new Object();
		this.controller.listenForLot(car, new Callback<Void, Lot>() {
			
			@Override
			public Void call(Lot lot) {
				object = lot;
				synchronized(finished) {
					finished.notify();
				}
				return null;
			}
		});
		
		this.controller.addLot(car);
		synchronized(finished) {
			finished.wait();
		}
		
		Car retrievedCar = (Car) this.object;
		assertNotSame(car, retrievedCar);
		assertEquals(car.make, retrievedCar.make);
		assertEquals(car.model, retrievedCar.model);
		this.space.take(car, null, Lease.FOREVER);
	}
	
	

	@Override
	public void init(AuctionController controller) {
		this.controller = controller;
		
	}
}
