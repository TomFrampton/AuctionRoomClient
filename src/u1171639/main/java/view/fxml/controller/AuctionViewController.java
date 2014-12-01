package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;

public class AuctionViewController extends ViewController {
	@FXML private Tab buyingTab;
	@FXML private Tab sellingTab;
	
	private ViewController buyingView;
	private ViewController sellingView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.buyingView = this.loadView("buying.fxml");
		this.sellingView = this.loadView("selling.fxml");
		
		this.buyingTab.setContent(this.buyingView.getViewComponent());
		this.sellingTab.setContent(this.sellingView.getViewComponent());
	}

}
