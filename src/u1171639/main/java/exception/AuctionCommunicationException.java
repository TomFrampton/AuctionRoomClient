package u1171639.main.java.exception;

public class AuctionCommunicationException extends AuctionException {

	public AuctionCommunicationException() {
		this("An error occured when communicating with the auction server");			
	}
	
	public AuctionCommunicationException(String error) {
		super(error);
	}
}
