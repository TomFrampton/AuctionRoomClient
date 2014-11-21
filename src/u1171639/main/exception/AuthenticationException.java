package u1171639.main.exception;

public class AuthenticationException extends Exception {
	private String error;
	
	public AuthenticationException() {
		
	}
	
	public AuthenticationException(String error) {
		this.error = error;
	}
	
	@Override
	public String toString() {
		return this.error;
	}
}
