package u1171639.main.java.view;

import u1171639.main.java.controller.AuctionController;
import u1171639.main.java.view.fxml.controller.ViewController;
import u1171639.main.java.view.fxml.utilities.FXMLView;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

public class FXApplicationStart extends Application {
	private static AuctionController controllerInstance;
	
	public static void setControllerInstance(AuctionController controller) {
		FXApplicationStart.controllerInstance = controller;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		FXMLView mainView = ViewController.loadView("main.fxml", FXApplicationStart.controllerInstance);
		
		Scene scene = new Scene(mainView.getComponent(), 800, 600);
		stage.setScene(scene);
		stage.show();
	}
}
