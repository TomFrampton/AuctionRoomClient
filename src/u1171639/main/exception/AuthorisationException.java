package u1171639.main.exception;

public class AuthorisationException extends Exception {
	private String error;
	
	public AuthorisationException() {
		
	}
	
	public AuthorisationException(String error) {
		this.error = error;
	}
	
	@Override
	public String toString() {
		return this.error;
	}
}
