package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;
//import jfx.messagebox.MessageBox;

public class SellingViewController extends ViewController {
	@FXML private Pane lotPane;
	@FXML private Pane bidsPane; 
	
	@FXML private TableView<Lot> yourLots;
	
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
		
		this.yourLots.getColumns().addAll(SellingViewController.getColumns(this.yourLots));
		this.yourLots.setItems(this.yourRetrivedLots);
		this.yourLots.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
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
			public Void call(Lot updatedLot) {
				// Update the lot in your lot list
				Lot lotToUpdate = null;
				
				for(Lot lot : SellingViewController.this.yourRetrivedLots) {
					if(lot.id.equals(updatedLot.id)) {
						lotToUpdate = lot;
						break;
					}
				}
				
				if(lotToUpdate != null) {
					SellingViewController.this.yourRetrivedLots.set(SellingViewController.this.yourRetrivedLots.indexOf(lotToUpdate), updatedLot);
				}
				
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Lot Updated");
				alert.setHeaderText("Lot Updated");
				alert.setContentText("Lot '" + updatedLot.name + "' has been updated successfully.");
				alert.show();
				
				return null;
			}
		});
		
		this.bidsController.setLotWithdrawnCallback(new Callback<Lot, Void>() {
			@Override
			public Void call(Lot withdrawnLot) {
				SellingViewController.this.lotPane.getChildren().clear();
				SellingViewController.this.bidsPane.getChildren().clear();
				
				Lot lotToRemove = null;
				
				for(Lot lot : SellingViewController.this.yourRetrivedLots) {
					if(lot.id.equals(withdrawnLot.id)) {
						lotToRemove = lot;
						break;
					}
				}
				
				if(lotToRemove != null) {
					SellingViewController.this.yourRetrivedLots.remove(lotToRemove);
				}
				
				return null;
			}
		});
		
		try {
			this.yourRetrivedLots.clear();
			this.yourRetrivedLots.addAll(getAuctionController().getUsersLots());
		} catch (RequiresLoginException e) {
			showErrorAlert(e);
		} catch (AuctionCommunicationException e) {
			showErrorAlert(e);
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
	
	public static ArrayList<TableColumn<Lot, ?>> getColumns(TableView<Lot> table) {
		ArrayList<TableColumn<Lot, ?>> columns = new ArrayList<>();
		
		TableColumn<Lot,String> lotAddedTime = new TableColumn<Lot,String>("Time Added");
		lotAddedTime.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Lot, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Lot, String> param) {
				return new SimpleStringProperty(param.getValue().timeAdded.toString());
				
			}
		});
		
		TableColumn<Lot,String> lotName = new TableColumn<Lot,String>("Lot Name");
		lotName.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Lot, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Lot, String> param) {
				return new SimpleStringProperty(param.getValue().name);
				
			}
		 });
		
		columns.add(lotAddedTime);
		columns.add(lotName);
		
		return columns;

	}
}
