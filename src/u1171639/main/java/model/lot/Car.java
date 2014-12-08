package u1171639.main.java.model.lot;

import net.sf.oval.constraint.MinLength;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

public class Car extends Lot {
	@NotNull
	@NotEmpty
	public String make;
	
	@NotNull
	@NotEmpty
	public String model;
}
