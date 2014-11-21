package u1171639.test;

import static org.junit.Assert.*;

import java.net.ConnectException;
import java.rmi.RemoteException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.controller.AuctionController;
import u1171639.main.model.lot.Car;
import u1171639.main.model.lot.Lot;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.HighestBid;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.SpaceUtils;
import u1171639.main.view.AuctionView;

public class DummyView implements AuctionView {

	private AuctionController controller;
	private Object object;
	private JavaSpace space;
	
	private static final String TESTING_FLAG = "TESTING";
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace();
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		LotService lotService = new JavaSpaceLotService(space);
		new AuctionController(this, lotService);
		
		LotIDCounter.initialiseInSpace(space);
	}
	
	

	@After
	public void tearDown() throws Exception {
		Lot template = new Lot();
		template.description = TESTING_FLAG;
		
		for(;;) {
			if(this.space.takeIfExists(template, null, 0) == null) {
				break;
			}
		}
	}
	
	

	@Test
	public void testAddLot() throws Exception {
		Car car = new Car();
		car.make = "Ford";
		car.model = "Focus";
		car.description = TESTING_FLAG;
		
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
	
	
	
	@Test
	public void testSubscribeToLot() throws Exception {
		Car car = new Car();
		car.make = "Mazda";
		car.model = "RX8";
		car.description = TESTING_FLAG;
		
		long carId = this.controller.addLot(car);
		
		final Object finished = new Object();
		
		this.controller.subscribeToLot(carId, new Callback<Void, Lot>() {
			
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
	
	
	
	@Test
	public void testListenForLot() throws Exception {
		Car car = new Car();
		car.make = "Honda";
		car.model = "Civic";
		car.description = TESTING_FLAG;
		
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
