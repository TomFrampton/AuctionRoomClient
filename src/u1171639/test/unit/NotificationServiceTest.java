package u1171639.test.unit;

import static org.junit.Assert.*;

import java.net.ConnectException;

import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationException;
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
import u1171639.test.utilities.TestUtils;

public class NotificationServiceTest {
	private NotificationService notificationService;
	private LotService lotService;
	
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
		this.lotService = new JavaSpaceLotService(this.space, transMgr);
		
		LotIDCounter.initialiseInSpace(this.space);
	}

	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new Notification(), this.space);
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
