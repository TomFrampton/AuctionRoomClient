package u1171639.main.java.utilities;

import net.jini.core.entry.Entry;

public class LotSubscription implements Entry {
	public Long userId;
	public Long lotId;
	
	public LotSubscription() {
		
	}
	
	public LotSubscription(long userId, long lotId) {
		this.userId = userId;
		this.lotId = lotId;
	}
}
