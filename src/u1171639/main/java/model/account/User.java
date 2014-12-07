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
	
	public User(String username) {
		this.username = username;
	}
	
	public User(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
