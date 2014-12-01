package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.utilities.FXMLView;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;

public class SellingViewController extends ViewController {
	private FXMLView addLotView;
	private FXMLView updateLotView;
	private FXMLView bidsView;
	
	@FXML private BorderPane sellingPane;
	@FXML private ListView<Lot> yourLots;
	
	private ObservableList<Lot> yourRetrivedLots = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.addLotView = this.loadView("addLot.fxml");
		this.updateLotView = this.loadView("updateLot.fxml");
		this.bidsView = this.loadView("bids.fxml");
		
		AddLotViewController addController = (AddLotViewController) this.addLotView.getController();
		addController.setLotSubmittedCallback(new Callback<Lot, Void>() {
			
			@Override
			public Void call(Lot param) {
				try {
					param.id = getAuctionController().addLot(param);
					SellingViewController.this.yourRetrivedLots.add(param);
					
					SellingViewController.this.sellingPane.setCenter(null);
					return null;
				} catch (RequiresLoginException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
			}
		});
		
		UpdateLotViewController updateController = (UpdateLotViewController) this.updateLotView.getController();
		updateController.setLotSubmittedCallback(new Callback<Lot, Void>() {
			
			@Override
			public Void call(Lot param) {
				try {
					getAuctionController().updateLot(param);
					
					SellingViewController.this.sellingPane.setCenter(null);
					return null;
				} catch (RequiresLoginException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					return null;
				}
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
			
			UpdateLotViewController updateController = (UpdateLotViewController) this.updateLotView.getController();
			updateController.setLotToUpdate(selected);
			
			this.sellingPane.setCenter(this.updateLotView.getComponent());
			this.sellingPane.setRight(this.bidsView.getComponent());
		}
	}
	
	@FXML protected void handleAddLotAction(ActionEvent event) {
		this.sellingPane.setCenter(this.addLotView.getComponent());
	}
}
