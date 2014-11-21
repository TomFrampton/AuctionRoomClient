package u1171639.main.model.lot;

import java.util.Date;

import u1171639.main.model.DistributedObject;

import net.jini.core.entry.Entry;

public class Lot implements DistributedObject {	
	public Long id;
	public String name;
	public String description;
	public Date timeAdded;
	public Long sellerId;
	
	public Lot() {
		
	}
	
	public Lot(long id) {
		this.id = id;
	}
}
