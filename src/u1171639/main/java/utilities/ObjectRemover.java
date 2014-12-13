package u1171639.main.java.utilities;

import java.net.ConnectException;

import u1171639.main.java.model.account.UserAccount;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.UserNotification;
import u1171639.main.java.utilities.counters.BidIDCounter;
import u1171639.main.java.utilities.counters.LotIDCounter;
import u1171639.main.java.utilities.counters.NotificationIDCounter;
import u1171639.main.java.utilities.counters.UserIDCounter;
import u1171639.main.java.utilities.flags.BidAcceptedFlag2;
import u1171639.main.java.utilities.flags.BidPlacedFlag;
import u1171639.main.java.utilities.flags.LotAddedFlag;
import u1171639.main.java.utilities.flags.LotRemovedFlag;
import u1171639.main.java.utilities.flags.LotUpdatedFlag;
import u1171639.main.java.utilities.flags.NotificationAddedFlag;
import u1171639.test.utilities.TestUtils;
import net.jini.space.JavaSpace;

public class ObjectRemover {
	public static void main(String args[]) throws ConnectException {
		JavaSpace space = SpaceUtils.getSpace(SpaceConsts.HOST);
		if(space == null) {
			throw new ConnectException("Could not connect to JavaSpace");
		}
		
		TestUtils.removeAllFromSpace(new Lot(), space);
		TestUtils.removeAllFromSpace(new Bid(), space);
		TestUtils.removeAllFromSpace(new UserNotification(), space);
		TestUtils.removeAllFromSpace(new UserAccount(), space);
		
		TestUtils.removeAllFromSpace(new BidAcceptedFlag2(), space);
		TestUtils.removeAllFromSpace(new BidPlacedFlag(), space);
		TestUtils.removeAllFromSpace(new LotAddedFlag(), space);
		TestUtils.removeAllFromSpace(new LotRemovedFlag(), space);
		TestUtils.removeAllFromSpace(new LotUpdatedFlag(), space);
		TestUtils.removeAllFromSpace(new NotificationAddedFlag(), space);
		
		TestUtils.removeAllFromSpace(new BidIDCounter(), space);
		TestUtils.removeAllFromSpace(new LotIDCounter(), space);
		TestUtils.removeAllFromSpace(new NotificationIDCounter(), space);
		TestUtils.removeAllFromSpace(new UserIDCounter(), space);
		
	}
}
