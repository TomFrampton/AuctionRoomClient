package u1171639.main.java.model.lot;

import java.math.BigDecimal;
import java.util.Date;

import u1171639.main.java.model.account.UserAccount;
import net.jini.core.entry.Entry;
import net.sf.oval.constraint.NotNull;

public class Bid implements Entry {
	public Long id;
	public Long lotId;
	public Long bidderId;
	
	@NotNull
	public BigDecimal amount;
	public Date bidTime;
	
	@NotNull
	public Boolean privateBid;
	
	public transient Lot lot;
	public transient UserAccount bidder;
	
	public Bid() {
		
	}
	
	public Bid(long id) {
		this.id = id;
	}
	
	public Bid(long id, long lotId) {
		this.id = id;
		this.lotId = lotId;
	}
	
	public Bid(long lotId, BigDecimal amount, Boolean privateBid) {
		this.lotId = lotId;
		this.amount = amount;
		this.privateBid = privateBid;
	}
	
	public Bid(long lotId, BigDecimal amount, long bidderId, Boolean privateBid) {
		this(lotId, amount, privateBid);
		this.bidderId = bidderId;
	}
}
