package u1171639.test.unit;

import static org.junit.Assert.*;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.List;

import net.jini.core.entry.UnusableEntryException;
import net.jini.core.event.RemoteEvent;
import net.jini.core.event.RemoteEventListener;
import net.jini.core.event.UnknownEventException;
import net.jini.core.lease.Lease;
import net.jini.core.transaction.TransactionException;
import net.jini.core.transaction.server.TransactionManager;
import net.jini.space.AvailabilityEvent;
import net.jini.space.JavaSpace;
import net.jini.space.JavaSpace05;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.service.JavaSpaceLotService;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.utilities.SpaceUtils;
import u1171639.main.java.utilities.counters.BidIDCounter;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.test.utilities.TestUtils;

public class RegTest {
	private JavaSpaceLotService lotService;
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
		
		LotIDCounter.initialiseInSpace(this.space);
		BidIDCounter.initialiseInSpace(this.space);
		
		this.lotService = new JavaSpaceLotService(this.space, transMgr);
	}

	@After
	public void tearDown() throws Exception {
		TestUtils.removeAllFromSpace(new Bid(), this.space);
		TestUtils.removeAllFromSpace(new Lot(), this.space);
	}

	@Test
	public void test() throws AuctionCommunicationException, UnauthorisedBidException, InvalidBidException, LotNotFoundException, RemoteException, TransactionException, InterruptedException {
		Car car = new Car();
		car.name = "Ford";
		car.model = "Fiesta";
		car.sellerId = 0l;
		
		car.id = this.lotService.addLot(car, null);
		
		RemoteEventListener listener = new RemoteEventListener() {
			@Override
			public void notify(RemoteEvent theEvent) throws UnknownEventException, RemoteException {
				AvailabilityEvent event = (AvailabilityEvent) theEvent;
				
				try {
					// AvailabilityEvent provides an extra method
					Bid lot = (Bid) event.getEntry();
					
					System.out.println("Lot with ID " + lot.id + " retrieved!\n");
					
				} catch (UnusableEntryException e) {
					System.err.println(e.getMessage());
				}
				
			}
		};
		
		List<Bid> templates = new ArrayList<Bid>();
		Bid template = new Bid();
		template.lotId = car.id;
		templates.add(template);
		
		UnicastRemoteObject.exportObject(listener, 0);
		JavaSpace05 space05 = (JavaSpace05) space;
		
		space05.registerForAvailabilityEvent(templates, null, false, listener, 1000000, null);	
	
		Bid bid = new Bid();
		bid.lotId = car.id;
		bid.amount = new BigDecimal("1000.00");
		bid.bidderId = 1l;

		this.lotService.bidForLot(bid);
		
		Thread.sleep(51000);
	}

}
