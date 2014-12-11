package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;

import org.controlsfx.control.action.Action;
import org.controlsfx.dialog.Dialogs;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ListView;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class BuyingViewController extends ViewController {
	@FXML private Pane lotPane;
	@FXML private Pane bidsPane;
	
	@FXML private TableView<Lot> lotList;
	
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
		
		this.lotList.getColumns().addAll(BuyingViewController.getColumns(this.lotList));
		this.lotList.setItems(this.retrievedLots);
		this.lotList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		this.searchLotsController.setSearchReturnedCallback(new Callback<List<Lot>, Void>() {
			
			@Override
			public Void call(List<Lot> lotsFound) {
				retrievedLots.clear();
				retrievedLots.addAll(lotsFound);
				
				if(lotsFound.isEmpty()) {
					Alert alert = new Alert(AlertType.INFORMATION);
					alert.setTitle("No Lots Found");
					alert.setHeaderText("No Lots Found");
					alert.setContentText("No Lots were found that match your search.");
					alert.show();
				}
				
				return null;
			}
		});
	}
	
	@FXML protected void handleLotSelected(MouseEvent event) {
		if(this.lotList.getSelectionModel().getSelectedIndex() >= 0) {
			Lot selectedLot = this.lotList.getSelectionModel().getSelectedItem();
			
			// Get the Lot from the server to ensure that it still exists
			try {
				Lot retrievedLot = getAuctionController().getLotDetails(selectedLot.id);
				
				this.viewLotController.setLotToView(retrievedLot);
				this.bidsController.setLotForBids(retrievedLot);

				this.lotPane.getChildren().clear();
				this.lotPane.getChildren().add(this.viewLotController.getViewComponent());
				
				this.bidsPane.getChildren().clear();
				this.bidsPane.getChildren().add(this.bidsController.getViewComponent());
				
			} catch (LotNotFoundException e) {
				// Remove Lot from the list
				this.retrievedLots.remove(selectedLot);

				if(this.viewLotController.getLotToView().id.equals(selectedLot.id)){
					this.lotPane.getChildren().clear();
					this.bidsPane.getChildren().clear();
				}
				
				showErrorAlert(e);
			} catch (RequiresLoginException e) {
				showErrorAlert(e);
			} catch (AuctionCommunicationException e) {
				showErrorAlert(e);
			}
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
		
		TableColumn<Lot,String> sellerName = new TableColumn<Lot,String>("Seller");
		sellerName.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Lot, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Lot, String> param) {
				return new SimpleStringProperty(param.getValue().seller.username);
				
			}
		 });
		
		columns.add(lotAddedTime);
		columns.add(lotName);
		columns.add(sellerName);
		
		return columns;

	}
}
