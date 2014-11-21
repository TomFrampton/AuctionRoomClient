package u1171639.main.model.account;

import u1171639.main.model.DistributedObject;
import net.jini.core.entry.Entry;

public class User extends Person implements DistributedObject {
	public Long id;
	public String email;
	public String username;
	public String password;
	public String salt;
	
	public User() {
		
	}
	
	public User(long id) {
		this.id = id;
	}
	
	public User(String email, String username, String password) {
		this.email = email;
		this.username = username;
		this.password = password;
	}
	
	public void hashPassword() {
		//https://crackstation.net/hashing-security.htm
	}
	
	private static String generateSalt() {
		return "";
	}
}
