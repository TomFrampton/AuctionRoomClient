package u1171639.main.java.utilities;

import net.jini.core.entry.Entry;

public class NotificationSubscription implements Entry {
	public Long userId;
	public Long lotId;
	
	public NotificationSubscription() {
		
	}
	
	public NotificationSubscription(long userId, long lotId) {
		this.userId = userId;
		this.lotId = lotId;
	}
}
