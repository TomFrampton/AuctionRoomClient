package u1171639.test;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import u1171639.main.controller.AuctionController;
import u1171639.main.view.AuctionView;

public class DummyView implements AuctionView {

	private AuctionController controller;
	
	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() {
		fail("Not yet implemented");
	}

	@Override
	public void init(AuctionController controller) {
		this.controller = controller;
		
	}

}
