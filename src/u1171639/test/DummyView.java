package u1171639.test;

import static org.junit.Assert.*;

import java.net.ConnectException;

import net.jini.space.JavaSpace;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.controller.AuctionController;
import u1171639.main.model.Car;
import u1171639.main.model.Lot;
import u1171639.main.service.JavaSpaceLotService;
import u1171639.main.service.LotService;
import u1171639.main.utilities.LotIDCounter;
import u1171639.main.utilities.SpaceUtils;
import u1171639.main.view.AuctionView;

public class DummyView implements AuctionView {

	private AuctionController controller;
	
	@Before
	public void setUp() throws Exception {
		JavaSpace space = SpaceUtils.getSpace();
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
		
		int carId = this.controller.addLot(car);
		assertTrue(carId >= 0);
		
		int carId2 = this.controller.addLot(car);
		assertTrue(carId2 == carId + 1);
	}

	@Override
	public void init(AuctionController controller) {
		this.controller = controller;
		
	}

}
