package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.utilities.FXMLView;
import javafx.fxml.FXML;
import javafx.scene.layout.BorderPane;

public class MainViewController extends ViewController {
	@FXML private BorderPane mainLayout;
	
	private FXMLView loginView;
	private FXMLView auctionView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loginView = loadView("login.fxml");
		//this.auctionView = loadView("auction.fxml");
		
		LoginViewController loginController = (LoginViewController) this.loginView.getController();
		
		loginController.setLoginSuccessCallback(new Callback<Object, Void>() {
			@Override
			public Void call(Object param) {
				MainViewController.this.mainLayout.setCenter(MainViewController.this.auctionView.getComponent());
				return null;
			}
		});
		
		this.mainLayout.setCenter(this.loginView.getComponent());
	}
}
