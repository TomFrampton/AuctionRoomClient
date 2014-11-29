package u1171639.main.java.utilities;

import java.security.SecureRandom;
import java.util.Random;

public class MediumSecurityHashScheme implements PasswordHashScheme {

	@Override
	public String generateSalt() {
		//http://stackoverflow.com/questions/18268502/how-to-generate-salt-value-in-java
		final Random random = new SecureRandom();
		byte[] salt = new byte[32];
		random.nextBytes(salt);
		return new String(salt);
	}

	@Override
	public String hashPassword(String password, String salt) {
		return password + salt;
	}
	
}
