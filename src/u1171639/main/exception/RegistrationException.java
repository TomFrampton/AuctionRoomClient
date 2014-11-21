package u1171639.main.exception;

public class RegistrationException extends Exception {
	private String error;
	
	public RegistrationException() {
		
	}
	
	public RegistrationException(String error) {
		this.error = error;
	}
	
	@Override
	public String toString() {
		return this.error;
	}
}
