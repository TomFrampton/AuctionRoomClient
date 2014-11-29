package u1171639.main.java.exception;

public class AuctionException extends Exception {
	private String error;
	
	public AuctionException(String error) {
		this.error = error;
	}
	
	@Override
	public String toString() {
		return this.error;
	}
}
