package u1171639.main.java.model.account;

import net.jini.core.entry.Entry;

public class User extends Person implements Entry {
	public Long id;
	public String username;
	public String password;
	public String salt;
	
	public User() {
		
	}
	
	public User(long id) {
		this.id = id;
	}
	
	public User(String email) {
		this.username = email;
	}
	
	public User(String email, String password) {
		this.username = email;
		this.password = password;
	}
}
