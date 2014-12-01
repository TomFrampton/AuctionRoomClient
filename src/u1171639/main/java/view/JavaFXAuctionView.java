package u1171639.main.java.view;

import u1171639.main.java.controller.AuctionController;
import javafx.application.Application;

public class JavaFXAuctionView implements AuctionView {

	public JavaFXAuctionView() {
		
	}
	
	@Override
	public void start(AuctionController controller) {
		FXApplicationStart.setControllerInstance(controller);
		Application.launch(FXApplicationStart.class);
	}
}
