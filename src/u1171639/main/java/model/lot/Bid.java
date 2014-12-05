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
	
	public transient Lot lot;
	public transient User bidder;
	
	public Bid() {
		
	}
	
	public Bid(long id) {
		this.id = id;
	}
	
	public Bid(long id, long lotId) {
		this.id = id;
		this.lotId = lotId;
	}
	
	public Bid(long lotId, BigDecimal amount, boolean privateBid) {
		this.lotId = lotId;
		this.amount = amount;
		this.privateBid = privateBid;
	}
	
	public Bid(long lotId, BigDecimal amount, long bidderId, boolean privateBid) {
		this(lotId, amount, privateBid);
		this.bidderId = bidderId;
	}
}
