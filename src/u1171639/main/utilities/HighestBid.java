package u1171639.main.utilities;

import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;

public class HighestBid implements Entry {
	public Long lotId;
	public Long bidId;
	
	public HighestBid() {
		
	}
	
	public HighestBid(long lotId) {
		this.lotId = lotId;
	}
}
