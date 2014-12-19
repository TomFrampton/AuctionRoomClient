package u1171639.main.java.utilities;

import java.security.SecureRandom;
import java.util.Random;

public class MediumSecurityHashScheme implements PasswordHashScheme {

	@Override
	public String generateSalt() {
		final Random random = new SecureRandom();
		byte[] salt = new byte[32];
		random.nextBytes(salt);
		return new String(salt);
	}

	@Override
	public String hashPassword(String password, String salt) {
		// Maybe this should be called LowSecurityHashScheme
		return password + salt;
	}
	
}
