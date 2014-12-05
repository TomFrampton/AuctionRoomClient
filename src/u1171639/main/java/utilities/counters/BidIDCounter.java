package u1171639.main.java.utilities.counters;

import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class BidIDCounter implements Entry {
	public Long id;
	
	public BidIDCounter() {
		
	}
	
	public BidIDCounter(long id) {
		this.id = id;
	}
	
	public void increment() {
		this.id++;
	}
	
	public static void initialiseInSpace(JavaSpace space) {
		try {
			BidIDCounter template = new BidIDCounter();
			BidIDCounter counter = (BidIDCounter) space.readIfExists(template, null, Lease.FOREVER);
			
			if(counter == null) {
				space.write(new BidIDCounter(0), null, Lease.FOREVER);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}