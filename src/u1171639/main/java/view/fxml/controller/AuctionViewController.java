package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;

public class AuctionViewController extends ViewController {
	@FXML private Tab buyingTab;
	@FXML private Tab sellingTab;
	@FXML private Tab accountTab;
	
	private ViewController buyingController;
	private ViewController sellingController;
	private ViewController accountView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.buyingController = this.loadView("buying.fxml");
		this.sellingController = this.loadView("selling.fxml");
		this.accountView = this.loadView("account.fxml");
		
		this.buyingTab.setContent(this.buyingController.getViewComponent());
		this.sellingTab.setContent(this.sellingController.getViewComponent());
		this.accountTab.setContent(this.accountView.getViewComponent());
	}

}
