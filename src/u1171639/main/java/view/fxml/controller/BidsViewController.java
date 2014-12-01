package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TableView;

public class BidsViewController extends ViewController {
	@FXML private TableView<Bid> bidList;
	
	private ObservableList<Bid> retrievedBids = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bidList.setItems(retrievedBids);
	}
	
	public void setLotForBids(Lot lot) {
		try {
			this.retrievedBids.clear();
			this.retrievedBids.addAll(getAuctionController().getVisibleBids(lot.id));
		} catch (RequiresLoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML protected void handleMakeBidAction(ActionEvent event) {
		
	}
	
	@FXML protected void handleAcceptHighestBidAction(ActionEvent event) {
		
	}
}
