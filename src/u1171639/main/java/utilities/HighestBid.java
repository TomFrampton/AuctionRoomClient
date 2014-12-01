package u1171639.main.java.utilities;

import net.jini.core.entry.Entry;

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
		return ++this.bidId;
	}
	
	public boolean hasBid() {
		return !this.bidId.equals(NO_BID_ID);
	}
}
