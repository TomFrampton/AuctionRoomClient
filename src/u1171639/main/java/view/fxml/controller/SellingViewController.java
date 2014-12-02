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
import javafx.scene.layout.Pane;
import jfx.messagebox.MessageBox;

public class SellingViewController extends ViewController {
	@FXML private Pane lotPane;
	@FXML private Pane bidsPane; 
	
	@FXML private ListView<Lot> yourLots;
	
	private AddLotViewController addLotController;
	private UpdateLotViewController updateLotController;
	private BidsViewController bidsController;
	
	private ObservableList<Lot> yourRetrivedLots = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.addLotController = (AddLotViewController) this.loadView("addLot.fxml");
		this.updateLotController = (UpdateLotViewController) this.loadView("updateLot.fxml");
		this.bidsController = (BidsViewController) this.loadView("bids.fxml");
		
		this.bidsController.showSellingFields();
		
		this.addLotController.setLotAddedCallback(new Callback<Lot, Void>() {
			@Override
			public Void call(Lot param) {	
				SellingViewController.this.yourRetrivedLots.add(param);
				SellingViewController.this.lotPane.getChildren().clear();
				return null;
			}
		});
		
		this.updateLotController.setLotUpdatedCallback(new Callback<Lot, Void>() {
			@Override
			public Void call(Lot param) {
				SellingViewController.this.lotPane.getChildren().clear();
				SellingViewController.this.bidsPane.getChildren().clear();
				return null;
			}
		});
		
		this.bidsController.setLotWithdrawnCallback(new Callback<Void, Void>() {
			@Override
			public Void call(Void param) {
				SellingViewController.this.lotPane.getChildren().clear();
				SellingViewController.this.bidsPane.getChildren().clear();
				
				SellingViewController.this.yourRetrivedLots.clear();
				try {
					SellingViewController.this.yourRetrivedLots.addAll(getAuctionController().getUsersLots());
				} catch (RequiresLoginException e) {
					MessageBox.show(SellingViewController.this.getWindow(), e.toString(), 
							"Error Withdrawing Lot", MessageBox.ICON_ERROR | MessageBox.OK);
				}
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
			this.bidsController.setLotForBids(selected);
			
			this.lotPane.getChildren().clear();
			this.lotPane.getChildren().add(this.updateLotController.getViewComponent());
			
			this.bidsPane.getChildren().clear();
			this.bidsPane.getChildren().add(this.bidsController.getViewComponent());
		}
	}
	
	@FXML protected void handleAddLotAction(ActionEvent event) {
		this.lotPane.getChildren().clear();
		this.lotPane.getChildren().add(this.addLotController.getViewComponent());
		
		this.bidsPane.getChildren().clear();
	}
}
