package u1171639.main.java.view;

import u1171639.main.java.controller.AuctionController;
import u1171639.main.java.view.fxml.controller.ViewController;
import u1171639.main.java.view.fxml.utilities.FXMLView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class JavaFXAuctionView implements AuctionView {

	public JavaFXAuctionView() {
		
	}
	
	@Override
	public void start(AuctionController controller) {
		FXApplicationStart.setControllerInstance(controller);
		Application.launch(FXApplicationStart.class);
	}
}
