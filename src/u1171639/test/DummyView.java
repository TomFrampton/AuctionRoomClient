package u1171639.test;

import static org.junit.Assert.*;

import java.net.ConnectException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.controller.AuctionController;
import u1171639.main.model.Car;
import u1171639.main.model.Lot;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.Callback;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.SpaceUtils;
import u1171639.main.view.AuctionView;

public class DummyView implements AuctionView {

	private AuctionController controller;
	private Object object;
	
	@Before
	public void setUp() throws Exception {
		JavaSpace space = SpaceUtils.getSpace("localhost");
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		LotService lotService = new JavaSpaceLotService(space);
		AuctionController controller = new AuctionController(this, lotService);
		
		LotIDCounter.initialiseInSpace(space);
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAddLot() {
		Car car = new Car();
		car.make = "Ford";
		car.model = "Focus";
		
		long carId = this.controller.addLot(car);
		assertTrue(carId >= 0);
		
		long carId2 = this.controller.addLot(car);
		assertTrue(carId2 == carId + 1);
	}
	
	@Test
	public void testListenForLot() throws InterruptedException, ExecutionException {
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
		
		assertNotEquals(car, retrievedCar);
		assertEquals(car.make, retrievedCar.make);
		assertEquals(car.model, retrievedCar.model);
	}

	@Override
	public void init(AuctionController controller) {
		this.controller = controller;
		
	}
}
