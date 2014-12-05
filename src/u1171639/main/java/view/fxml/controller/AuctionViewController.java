package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;

public class AuctionViewController extends ViewController {
	@FXML private Tab buyingTab;
	@FXML private Tab sellingTab;
	@FXML private Tab accountTab;
	@FXML private Tab notificationsTab;
	
	private ViewController buyingController;
	private ViewController sellingController;
	private ViewController accountController;
	private NotificationsViewController notificationsController;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.buyingController = this.loadView("buying.fxml");
		this.sellingController = this.loadView("selling.fxml");
		this.accountController = this.loadView("account.fxml");
		this.notificationsController = (NotificationsViewController) this.loadView("notifications.fxml");
		
		// Pass a reference to the notificationController of its tab so it can change the text value
		// based on how many notifications are pending.
		this.notificationsController.setNotificationTab(notificationsTab);
		
		this.buyingTab.setContent(this.buyingController.getViewComponent());
		this.sellingTab.setContent(this.sellingController.getViewComponent());
		this.accountTab.setContent(this.accountController.getViewComponent());
		this.notificationsTab.setContent(this.notificationsController.getViewComponent());
	}
}
