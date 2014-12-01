package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.utilities.FXMLView;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class MainViewController extends ViewController {
	@FXML private BorderPane mainLayout;
	
	private FXMLView loginView;
	private FXMLView registerView;
	
	private FXMLView auctionView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loginView = loadView("login.fxml");
		this.registerView = loadView("register.fxml");
		
		HBox layout = new HBox();
		layout.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(
				loginView.getComponent(),
				new Separator(),
				registerView.getComponent());
		
		LoginViewController loginController = (LoginViewController) this.loginView.getController();
		
		loginController.setLoginSuccessCallback(new Callback<Object, Void>() {
			@Override
			public Void call(Object param) {
				MainViewController.this.auctionView = loadView("auction.fxml");
				MainViewController.this.mainLayout.setCenter(MainViewController.this.auctionView.getComponent());
				return null;
			}
		});
		
		this.mainLayout.setCenter(layout);
	}
}
