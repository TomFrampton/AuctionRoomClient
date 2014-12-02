package u1171639.test.unit;

import static org.junit.Assert.*;

import java.net.ConnectException;

import net.jini.core.entry.Entry;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.service.JavaSpaceNotificationService;
import u1171639.main.java.service.LotService;
import u1171639.main.java.service.NotificationService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.HighestBid;
import u1171639.main.java.utilities.LotIDCounter;
import u1171639.main.java.utilities.NotificationSubscription;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.test.utilities.TestUtils;

public class NotificationServiceTest {
	private NotificationService notificationService;
	private LotService lotService;
	
	private JavaSpace space;
	
	@Before
	public void setUp() throws Exception {
		this.space = SpaceUtils.getSpace("localhost");
		if(this.space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TransactionManager transMgr = SpaceUtils.getManager("localhost");
		if(transMgr == null) {
			throw new ConnectException("Could not connect to TransactionManager");
		}
		
		this.notificationService = new JavaSpaceNotificationService(this.space, transMgr);
		this.lotService = new JavaSpaceLotService(this.space, transMgr);
		
		LotIDCounter.initialiseInSpace(this.space);
	}

	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new Bid(), this.space);
		TestUtils.removeAllFromSpace(new Lot(), this.space);
		TestUtils.removeAllFromSpace(new NotificationSubscription(), this.space);
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
		
		try {
			this.notificationService.subscribeToLot(car.id, 0l, new Callback<Lot, Void>() {
				@Override
				public Void call(Lot changedLot) {
					lot[0] = changedLot;
					
					synchronized(finished) {
						finished.notify();
					}
					return null;
				}
			});
		} catch (NotificationException e) {
			fail("Was not already subscribed to Lot.");
		} catch (LotNotFoundException e) {
			fail("Lot was added. Should have been found.");
		}
		
		try {
			this.notificationService.subscribeToLot(car.id, 0l, new Callback<Lot, Void>() {
				@Override
				public Void call(Lot changedLot) {
					return null;
				}
			});
			fail("Lot already subscribed to. Exception should have been thrown.");
		} catch (NotificationException e) {
			// Pass
		} catch (LotNotFoundException e) {
			fail("Lot was added. Should have been found.");
		}
		
		this.lotService.updateLot(car);
		this.waitForNotification(finished);
	}
	
	@Test
	public void testListenForLot() {
		Car car = new Car();
		car.make = "UnitTest"; 
		car.model = "ListenForLot";
		
		final Object finished = new Object();
		
		// Used for returning the lot in the callback
		final Lot[] lot = new Lot[1];
		
		this.notificationService.listenForLot(car, 0l, new Callback<Lot, Void>() {
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
