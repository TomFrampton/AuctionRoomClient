package u1171639.main.java.model.account;

import java.util.Date;

public class Person {
	public String forename;
	public String surname;
	public Date dob;
	
	public Person() {
		
	}

	public Person(String forename, String surname, Date dob) {
		this.forename = forename;
		this.surname = surname;
		this.dob = dob;
	}
}
