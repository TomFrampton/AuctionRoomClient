package u1171639.main.java.model.lot;

import java.math.BigDecimal;
import java.util.Date;

import u1171639.main.java.model.account.User;
import net.jini.core.entry.Entry;

public class Bid implements Entry {
	public Long id;
	public Long lotId;
	public Long bidderId;
	public BigDecimal amount;
	public Date bidTime;
	public Boolean privateBid;
	
	public transient User bidder;
	
	public Bid() {
		
	}
	
	public Bid(long id, long lotId) {
		this.id = id;
		this.lotId = lotId;
	}
	
	public Bid(long id, long lotId, long bidderId, BigDecimal amount) {
		this(id, lotId);
		this.bidderId = bidderId;
		this.amount = amount;
	}
	
	public Bid(long id, long lotId, long bidderId, BigDecimal amount, boolean privateBid) {
		this(id, lotId, bidderId, amount);
		this.privateBid = privateBid;
	}
}
