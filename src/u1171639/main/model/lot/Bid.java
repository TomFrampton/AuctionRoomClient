package u1171639.main.model.lot;

import java.math.BigDecimal;
import java.util.Date;

import net.jini.core.entry.Entry;

public class Bid implements Entry {
	public long id;
	public long lotId;
	public BigDecimal amount;
	public Date bidTime;
	
	public Bid() {
		
	}
	
	public Bid(long id) {
		this.id = id;
	}
}
