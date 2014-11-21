package u1171639.main.model.lot;

import java.math.BigDecimal;
import java.util.Date;

import u1171639.main.model.DistributedObject;

import net.jini.core.entry.Entry;

public class Bid implements DistributedObject {
	public long id;
	public long lotId;
	public BigDecimal amount;
	public Date bidTime;
	
	public Bid() {
		
	}
	
	public Bid(long id, long lotId) {
		this.id = id;
		this.lotId = lotId;
	}
	
	public Bid(long id, long lotId, BigDecimal amount) {
		
	}
}
