package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import u1171639.main.java.view.fxml.utilities.FXMLView;

public class AuctionViewController extends ViewController {
	@FXML private Tab buyingTab;
	@FXML private Tab sellingTab;
	
	private FXMLView buyingView;
	private FXMLView sellingView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.buyingView = this.loadView("buying.fxml");
		this.sellingView = this.loadView("selling.fxml");
		
		this.buyingTab.setContent(buyingView.getComponent());
		this.sellingTab.setContent(sellingView.getComponent());
	}

}
