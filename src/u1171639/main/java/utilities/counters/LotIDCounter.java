package u1171639.main.java.utilities.counters;

import u1171639.main.java.service.LotService;
import u1171639.main.java.utilities.SpaceConsts;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class LotIDCounter implements Entry {
	public Long id;
	
	public LotIDCounter() {
		
	}
	
	public LotIDCounter(long id) {
		this.id = id;
	}
	
	public void increment() {
		this.id++;
	}
	
	public static void initialiseInSpace(JavaSpace space) {
		try {
			LotIDCounter template = new LotIDCounter();
			LotIDCounter counter = (LotIDCounter) space.readIfExists(template, null, SpaceConsts.WAIT_TIME);
			
			if(counter == null) {
				space.write(new LotIDCounter(0), null, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
