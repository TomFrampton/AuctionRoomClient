package u1171639.main.utilities;

import net.jini.core.entry.Entry;
import net.jini.space.JavaSpace;

public class HighestBid implements Entry {
	public Long lotId;
	public Long bidId;
	public static final Long NO_BID_ID = -1l;
	
	public HighestBid() {
		
	}
	
	public HighestBid(long lotId) {
		this.lotId = lotId;
	}
	
	public long nextBidId() {
		return ++bidId;
	}
	
	public boolean hasBid() {
		return !bidId.equals(NO_BID_ID);
	}
}
