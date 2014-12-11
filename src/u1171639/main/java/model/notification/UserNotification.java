package u1171639.main.java.model.notification;

import java.util.Date;

import net.jini.core.entry.Entry;

public class UserNotification implements Entry {
	public Long id;
	public Long recipientId;
	
	public String title;
	public String message;
	
	public Date timeReceived;
	public Boolean read;
	
	public UserNotification() {
		
	}
	
	public UserNotification(long id) {
		this.id = id;
	}
}
