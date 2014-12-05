package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class BuyingViewController extends ViewController {
	@FXML private Pane lotPane;
	@FXML private Pane bidsPane;
	
	@FXML private ListView<Lot> lotList;
	
	private ViewLotViewController viewLotController;
	private SearchLotsViewController searchLotsController;
	private BidsViewController bidsController;
	
	final ObservableList<Lot> retrievedLots = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.viewLotController = (ViewLotViewController) this.loadView("viewLot.fxml");
		this.searchLotsController = (SearchLotsViewController) this.loadView("searchLots.fxml");
		this.bidsController = (BidsViewController) this.loadView("bids.fxml");
		this.bidsController.showBuyingFields();
		
		this.searchLotsController.setSearchReturnedCallback(new Callback<List<Lot>, Void>() {
			
			@Override
			public Void call(List<Lot> lotsFound) {
				BuyingViewController.this.retrievedLots.clear();
				BuyingViewController.this.retrievedLots.addAll(lotsFound);
				return null;
			}
		});
		
		
		this.lotList.setItems(this.retrievedLots);
	}
	
	@FXML protected void handleLotSelected(MouseEvent event) {
		if(this.lotList.getSelectionModel().getSelectedIndex() >= 0) {
			Lot selectedLot = this.lotList.getSelectionModel().getSelectedItem();
			
			this.viewLotController.setLotToView(selectedLot);
			this.bidsController.setLotForBids(selectedLot);

			this.lotPane.getChildren().clear();
			this.lotPane.getChildren().add(this.viewLotController.getViewComponent());
			
			this.bidsPane.getChildren().clear();
			this.bidsPane.getChildren().add(this.bidsController.getViewComponent());
		}
	}
	
	@FXML protected void handleEditSearch(MouseEvent event) {
		this.lotPane.getChildren().clear();
		this.lotPane.getChildren().add(this.searchLotsController.getViewComponent());
		
		this.bidsPane.getChildren().clear();
	}
	
	@FXML protected void handleClearSearch(MouseEvent event) {
		this.retrievedLots.clear();
		this.lotPane.getChildren().clear();
		this.bidsPane.getChildren().clear();
	}
}
