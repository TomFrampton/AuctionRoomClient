package u1171639.main.java.model.account;

import java.beans.Transient;

import com.sun.org.glassfish.gmbal.NameValue;

import net.jini.core.entry.Entry;
import net.sf.oval.constraint.MaxLength;
import net.sf.oval.constraint.MinLength;
import net.sf.oval.constraint.NotBlank;
import net.sf.oval.constraint.NotEmpty;
import net.sf.oval.constraint.NotNull;

public class UserAccount extends Person implements Entry {
	public Long id;
	
	@NotNull
	@MinLength(5)
	@MaxLength(32)
	public String username;
	
	@NotNull
	@MinLength(5)
	@MaxLength(32)
	public String password;
	
	public String salt;
	
	public UserAccount() {
		
	}
	
	public UserAccount(long id) {
		this.id = id;
	}
	
	public UserAccount(String username) {
		this.username = username;
	}
	
	public UserAccount(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
