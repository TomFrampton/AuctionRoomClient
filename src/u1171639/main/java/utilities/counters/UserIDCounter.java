package u1171639.main.java.utilities.counters;

import u1171639.main.java.service.LotService;
import u1171639.main.java.utilities.SpaceConsts;
import net.jini.core.entry.Entry;
import net.jini.core.lease.Lease;
import net.jini.space.JavaSpace;

public class UserIDCounter implements Entry {
	public Long id;
	
	public UserIDCounter() {
		
	}
	
	public UserIDCounter(long id) {
		this.id = id;
	}
	
	public void increment() {
		this.id++;
	}
	
	public static void initialiseInSpace(JavaSpace space) {
		try {
			UserIDCounter template = new UserIDCounter();
			UserIDCounter counter = (UserIDCounter) space.readIfExists(template, null, SpaceConsts.WAIT_TIME);
			
			if(counter == null) {
				space.write(new UserIDCounter(0), null, SpaceConsts.WRITE_TIME);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
