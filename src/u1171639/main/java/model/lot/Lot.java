package u1171639.main.java.model.lot;

import java.util.Date;
import java.util.List;

import u1171639.main.java.model.account.UserAccount;
import net.jini.core.entry.Entry;
import net.sf.oval.constraint.MinLength;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

public class Lot implements Entry {	
	public Long id;
	
	@NotNull
	@NotEmpty
	@MinLength(5)
	public String name;
	
	@NotNull
	@NotEmpty
	@MinLength(10)
	public String description;
	
	public Date timeAdded;
	public Long sellerId;
	
	public transient List<Bid> bids;
	public transient UserAccount seller;
	
	public Lot() {
		
	}
	
	public Lot(long id) {
		this.id = id;
	}
	
	public enum Type {
		Car
	}
}
