package u1171639.main.java.model.lot;

import java.util.Date;
import java.util.List;

import u1171639.main.java.model.account.User;
import net.jini.core.entry.Entry;

public class Lot implements Entry {	
	public Long id;
	public String name;
	public String description;
	public Date timeAdded;
	public Long sellerId;
	
	public transient List<Bid> bids;
	public transient User seller;
	
	public Lot() {
		
	}
	
	public Lot(long id) {
		this.id = id;
	}
	
	public enum Type {
		Car
	}
}
