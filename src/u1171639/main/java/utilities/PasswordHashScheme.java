package u1171639.main.java.utilities;

public interface PasswordHashScheme {
	public String generateSalt();
	public String hashPassword(String password, String salt);
}
