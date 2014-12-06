package u1171639.main.java.model.notification;

import java.util.Date;

import net.jini.core.entry.Entry;

public class Notification implements Entry {
	public Long id;
	public Long recipientId;
	
	public String title;
	public String message;
	
	public Date timeReceived;
	public Boolean read;
	
	public Notification() {
		
	}
	
	public Notification(long id) {
		this.id = id;
	}
}
