package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.utilities.FXMLView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class BuyingViewController extends ViewController {
	@FXML private BorderPane buyingPane;
	@FXML private ListView<Lot> lotList;
	
	private FXMLView lotView;
	private FXMLView lotFormView;
	
	private VBox lotSearchPane = new VBox();
	
	final ObservableList<Lot> retrievedLots = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.lotView = this.loadView("lot.fxml");
		this.lotFormView = this.loadView("lotForm.fxml");
		
		LotFormViewController controller = (LotFormViewController) this.lotFormView.getController();
		controller.setLotSubmittedCallback(new Callback<Lot, Void>() {
			
			@Override
			public Void call(Lot param) {
				try {
					List<Lot> lotsFound = getAuctionController().searchLots(param);
					
					BuyingViewController.this.retrievedLots.clear();
					BuyingViewController.this.retrievedLots.addAll(lotsFound);
					return null;
				} catch (RequiresLoginException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		});
		
		
		this.lotList.setItems(retrievedLots);
		
		this.lotSearchPane.getChildren().addAll(
				new Text("Search For Lots"),
				this.lotFormView.getComponent());
	}
	
	@FXML protected void handleLotSelected(MouseEvent event) {
		if(this.lotList.getSelectionModel().getSelectedIndex() >= 0) {
			Lot selected = this.lotList.getSelectionModel().getSelectedItem();
			
			LotViewController lotController = (LotViewController) this.lotView.getController();
			lotController.setLot(selected);
			
			this.buyingPane.setRight(this.lotView.getComponent());
		}
	}
	
	@FXML protected void handleEditSearch(MouseEvent event) {
		this.buyingPane.setRight(this.lotSearchPane);
	}
	
	@FXML protected void handleClearSearch(MouseEvent event) {
		this.retrievedLots.clear();
		this.buyingPane.setRight(null);
	}
	

}
