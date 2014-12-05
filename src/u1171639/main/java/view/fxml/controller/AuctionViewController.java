package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.layout.Pane;

public class AuctionViewController extends ViewController {
	@FXML private Tab buyingTab;
	@FXML private Tab sellingTab;
	@FXML private Tab accountTab;
	//@FXML private Pane notificationsPane;
	
	private ViewController buyingController;
	private ViewController sellingController;
	private ViewController accountController;
	//private NotificationsViewController notificationsController;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.buyingController = this.loadView("buying.fxml");
		this.sellingController = this.loadView("selling.fxml");
		this.accountController = this.loadView("account.fxml");
		//this.notificationsController = (NotificationsViewController) this.loadView("notifications.fxml");
		
		this.buyingTab.setContent(this.buyingController.getViewComponent());
		this.sellingTab.setContent(this.sellingController.getViewComponent());
		this.accountTab.setContent(this.accountController.getViewComponent());
		//this.notificationsPane.getChildren().add(this.notificationsController.getViewComponent());
	}

}
