package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;

public class SellingViewController extends ViewController {
	private AddLotViewController addLotController;
	private UpdateLotViewController updateLotController;
	private BidsViewController bidsController;
	
	@FXML private BorderPane sellingPane;
	@FXML private ListView<Lot> yourLots;
	
	private ObservableList<Lot> yourRetrivedLots = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.addLotController = (AddLotViewController) this.loadView("addLot.fxml");
		this.updateLotController = (UpdateLotViewController) this.loadView("updateLot.fxml");
		this.bidsController = (BidsViewController) this.loadView("bids.fxml");
		
		this.addLotController.setLotAddedCallback(new Callback<Lot, Void>() {
			@Override
			public Void call(Lot param) {	
				SellingViewController.this.yourRetrivedLots.add(param);
				SellingViewController.this.sellingPane.setCenter(null);
				return null;
			}
		});
		
		this.updateLotController.setLotUpdatedCallback(new Callback<Lot, Void>() {
			@Override
			public Void call(Lot param) {
				SellingViewController.this.sellingPane.setCenter(null);
				return null;
			}
		});
		
		this.yourLots.setItems(this.yourRetrivedLots);
		
		try {
			this.yourRetrivedLots.clear();
			this.yourRetrivedLots.addAll(getAuctionController().getUsersLots());
		} catch (RequiresLoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	@FXML protected void handleLotSelected(MouseEvent event) {
		if(this.yourLots.getSelectionModel().getSelectedIndex() >= 0) {
			Lot selected = this.yourLots.getSelectionModel().getSelectedItem();
			
			this.updateLotController.setLotToUpdate(selected);
			
			this.sellingPane.setCenter(this.updateLotController.getViewComponent());
			this.sellingPane.setRight(this.bidsController.getViewComponent());
		}
	}
	
	@FXML protected void handleAddLotAction(ActionEvent event) {
		this.sellingPane.setCenter(this.addLotController.getViewComponent());
		this.sellingPane.setRight(null);
	}
}
