package u1171639.main.java.model.account;

import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

public class Person {
	@NotNull
	@NotEmpty
	public String forename;
	
	@NotNull
	@NotEmpty
	public String surname;
	
	public Person() {
		
	}

	public Person(String forename, String surname) {
		this.forename = forename;
		this.surname = surname;
	}
}
