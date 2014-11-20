package u1171639.main.model.account;

import java.util.Date;

public class Person {
	private String forename;
	private String surname;
	private Date dob;
	
	public Person() {
		
	}

	public Person(String forename, String surname, Date dob) {
		this.forename = forename;
		this.surname = surname;
		this.dob = dob;
	}

	public String getForename() {
		return forename;
	}

	public void setForename(String forename) {
		this.forename = forename;
	}

	public String getSurname() {
		return surname;
	}

	public void setSurname(String surname) {
		this.surname = surname;
	}

	public Date getDob() {
		return dob;
	}

	public void setDob(Date dob) {
		this.dob = dob;
	}
}
