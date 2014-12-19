package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.utilities.Callback;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.Separator;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;

public class AuthenticationViewController extends ViewController {
	@FXML private BorderPane mainLayout;
	
	private LoginViewController loginController;
	private RegisterViewController registerController;
	
	private AuctionViewController auctionController;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.loginController = (LoginViewController) loadView("login.fxml");
		this.registerController = (RegisterViewController) loadView("register.fxml");
		
		HBox layout = new HBox();
		layout.setAlignment(Pos.CENTER);
		layout.getChildren().addAll(
				this.loginController.getViewComponent(),
				new Separator(),
				this.registerController.getViewComponent());
			
		this.loginController.setLoginSuccessCallback(new Callback<Object, Void>() {
			
			@Override
			public Void call(Object param) {
				AuthenticationViewController.this.auctionController = (AuctionViewController) loadView("auction.fxml");
				AuthenticationViewController.this.mainLayout.setCenter(AuthenticationViewController.this.auctionController.getViewComponent());
				return null;
			}
		});
		
		this.mainLayout.setCenter(layout);
	}
}
