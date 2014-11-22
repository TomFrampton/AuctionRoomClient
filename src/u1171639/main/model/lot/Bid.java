package u1171639.main.model.lot;

import java.math.BigDecimal;
import java.util.Date;

import net.jini.core.entry.Entry;

public class Bid implements Entry {
	public Long id;
	public Long lotId;
	public BigDecimal amount;
	public Date bidTime;
	
	public Bid() {
		
	}
	
	public Bid(long id, long lotId) {
		this.id = id;
		this.lotId = lotId;
	}
	
	public Bid(long id, long lotId, BigDecimal amount) {
		this(id, lotId);
		this.amount = amount;
	}
}
