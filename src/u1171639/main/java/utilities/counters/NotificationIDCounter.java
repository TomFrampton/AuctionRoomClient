package u1171639.main.java.utilities.counters;

import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;
import u1171639.main.java.utilities.SpaceConsts;

public class NotificationIDCounter implements Entry {
	public Long id;
	
	public NotificationIDCounter() {
		
	}
	
	public NotificationIDCounter(long id) {
		this.id = id;
	}
	
	public void increment() {
		this.id++;
	}
	
	public static void initialiseInSpace(JavaSpace space) {
		try {
			NotificationIDCounter template = new NotificationIDCounter();
			NotificationIDCounter counter = (NotificationIDCounter) space.readIfExists(template, null, SpaceConsts.WAIT_TIME);
			
			if(counter == null) {
				space.write(new NotificationIDCounter(0), null, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
