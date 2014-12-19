package u1171639.main.java.view;

import java.io.File;

import u1171639.main.java.controller.AuctionController;
import u1171639.main.java.view.fxml.controller.AuthenticationViewController;
import u1171639.main.java.view.fxml.controller.ViewController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;

public class FXApplicationStart extends Application {
	private static AuctionController controllerInstance;
	
	private static boolean connectError = false;
	
	public static void setControllerInstance(AuctionController controller) {
		FXApplicationStart.controllerInstance = controller;
	}
	
	@Override
	public void start(Stage stage) throws Exception {
		AuthenticationViewController mainView = (AuthenticationViewController) ViewController.loadView("authentication.fxml", FXApplicationStart.controllerInstance);
		
		
		
		if(FXApplicationStart.connectError) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Could Not Connect to the Auction");
			alert.setHeaderText("Could Not Connect to the Auction");
			alert.setContentText("There was an error when attemping to connect to the auction server.");
			alert.show();
		} else {
			Scene scene = new Scene(mainView.getViewComponent(), 1200, 800);
			
			// Load CSS
			// Load all CSS files
			File cssDirectory = new File("./src/u1171639/main/resources/styles/");
			for(File cssFile : cssDirectory.listFiles()) {
				String css = this.getClass().getResource("/u1171639/main/resources/styles/" + cssFile.getName()).toExternalForm();
				scene.getStylesheets().add(css);
			}
			
			stage.setScene(scene);
			stage.show();
		}
		
		
		
	}
	
	public static void connectionError() {
		FXApplicationStart.connectError = true;
		Application.launch();
	}
}
