package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.utilities.FXMLView;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SellingViewController extends ViewController {
	private FXMLView lotFormView;
	
	@FXML private BorderPane sellingPane;
	@FXML private ListView<Lot> yourLots;
	
	private VBox addLotPane = new VBox();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.lotFormView = this.loadView("lotForm.fxml");
		
		LotFormViewController controller = (LotFormViewController) this.lotFormView.getController();
		controller.setLotSubmittedCallback(new Callback<Lot, Void>() {
			
			@Override
			public Void call(Lot param) {
				try {
					getAuctionController().addLot(param);
					return null;
				} catch (RequiresLoginException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		});
		
		this.addLotPane.getChildren().addAll(
				new Text("Add Lot For Sale"),
				this.lotFormView.getComponent());
	}
	
	@FXML protected void handleLotSelected(MouseEvent event) {
		if(this.yourLots.getSelectionModel().getSelectedIndex() >= 0) {
			Lot selected = this.yourLots.getSelectionModel().getSelectedItem();
			
			this.sellingPane.setRight(null);
		}
	}
	
	@FXML protected void handleAddLotAction(ActionEvent event) {
		this.sellingPane.setRight(this.addLotPane);
	}
}
