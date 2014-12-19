package u1171639.main.java.utilities.counters;

import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;
import u1171639.main.java.utilities.SpaceConsts;

public abstract class IDCounter implements Entry {
	public Long id;
	
	public void increment() {
		this.id++;
	}
	
	public static <T extends IDCounter> void initialiseInSpace(Class<T> type, JavaSpace space) {
		try {
			T template = type.newInstance();
			T counter = (T) space.readIfExists(template, null, SpaceConsts.WAIT_TIME);		
			
			if(counter == null) {
				T newCounter = type.newInstance();
				newCounter.id = 0l;
				space.write(newCounter, null, SpaceConsts.AUCTION_ENTITY_WRITE_TIME);
			}
		} catch(Exception e) {
			e.printStackTrace();
		}
	}
}
