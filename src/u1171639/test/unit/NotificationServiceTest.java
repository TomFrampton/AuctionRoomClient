package u1171639.test.unit;

import static org.junit.Assert.*;

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
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.UserNotFoundException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.service.JavaSpaceNotificationService;
import u1171639.main.java.service.LotService;
import u1171639.main.java.service.NotificationService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.LotSubscription;
import u1171639.main.java.utilities.SpaceConsts;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.main.java.utilities.counters.NotificationIDCounter;
import u1171639.main.java.utilities.flags.NotificationAddedFlag;
import u1171639.test.utilities.TestUtils;

public class NotificationServiceTest {
	private NotificationService notificationService;
	
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
		
		this.notificationService = new JavaSpaceNotificationService(this.space, transMgr);
		
		NotificationIDCounter.initialiseInSpace(this.space);
	}

	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new Notification(), this.space);
		TestUtils.removeAllFromSpace(new NotificationAddedFlag(), this.space);
		TestUtils.removeAllFromSpace(new NotificationIDCounter(), this.space);
	}
	
	@Test
	public void retrieveAllNotifications() {
		Notification notification1 = new Notification();
		notification1.title = "Testing Title 1";
		notification1.message = "Testing Message 1";
		notification1.recipientId = 0l;
		
		Notification notification2 = new Notification();
		notification2.title = "Testing Title 2";
		notification2.message = "Testing Message 2";
		notification2.recipientId = 0l;
		
		Notification notification3 = new Notification();
		notification3.title = "Testing Title 3";
		notification3.message = "Testing Message 3";
		notification3.recipientId = 1l;
		
		try {
			this.notificationService.addNotification(notification1);
			this.notificationService.addNotification(notification2);
			this.notificationService.addNotification(notification3);
			
			List<Notification> retrievedNotifications1 = this.notificationService.retrieveAllNotifications(0l);
			
			assertTrue(retrievedNotifications1.size() == 2);
			
			Collections.sort(retrievedNotifications1, new Comparator<Notification>() {
				@Override
				public int compare(Notification o1, Notification o2) {
					return Long.compare(o1.id, o2.id);
				}
			});
			
			assertEquals(retrievedNotifications1.get(0).title, "Testing Title 1");
			assertEquals(retrievedNotifications1.get(1).title, "Testing Title 2");
			
			
			List<Notification> retrievedNotifications2 = this.notificationService.retrieveAllNotifications(1l);
			
			assertTrue(retrievedNotifications2.size() == 1);
					
			assertEquals(retrievedNotifications2.get(0).title, "Testing Title 3");
			
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testListenForNotifications() {
		final Notification notification1 = new Notification();
		notification1.title = "Testing Title 1";
		notification1.message = "Testing Message 1";
		notification1.recipientId = 0l;
		
		final Object lock = new Object();
		
		try {
			this.notificationService.listenForNotifications(0l, new Callback<Notification, Void>() {

				@Override
				public Void call(Notification notification) {
					assertEquals(notification.title, notification1.title);
					assertEquals(notification.message, notification1.message);
					
					synchronized (lock) {
						lock.notify();
					}
					
					return null;
				}
			});
			
			this.notificationService.addNotification(notification1);
			this.waitForNotification(lock);
			
		} catch (AuctionCommunicationException e) {
			fail(e.getMessage());
		}
	}
	
	@Test
	public void testMarkNotificationRead() {
		Notification notification = new Notification();
		notification.recipientId = 0l;
		notification.title = "Testing Title";
		notification.message = "Testing Message";
		
		try {
			this.notificationService.addNotification(notification);
			
			Notification retrieved = this.notificationService.retrieveAllNotifications(0l).get(0);
			assertTrue(retrieved.read == false);
			
			this.notificationService.markNotificationRead(retrieved.id);
			
			retrieved = this.notificationService.retrieveAllNotifications(0l).get(0);
			assertTrue(retrieved.read == true);
			
		} catch (AuctionCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NotificationNotFoundException e) {
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
