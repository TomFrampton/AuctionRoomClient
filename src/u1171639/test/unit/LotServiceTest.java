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
	}

	@After
	public void tearDown() throws Exception {
	}

//	public void bidForLot(long lotId, BigDecimal amount, User bidder) throws UnauthorisedBidException, InvalidBidException;
//	public Bid getHighestBid(long lotId);
//	public Lot getLotDetails(long lotId);
//	public void subscribeToLot(long lotId, Callback<Void, Lot> callback);
//	public void listenForLot(Lot template, Callback<Void, Lot> callback);
	
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

}
