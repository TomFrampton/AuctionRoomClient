package u1171639.main.java.view.fxml.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.ResourceBundle;

import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.util.Callback;

public class BidsViewController extends ViewController {
	@FXML private TableView<Bid> bidList;
	
	@FXML private TextField amountPounds;
	@FXML private TextField amountPence;
	@FXML private ToggleGroup bidPrivacyGroup;
	
	private Lot lotForBids;
	
	private ObservableList<Bid> retrievedBids = FXCollections.observableArrayList();
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bidList.getColumns().addAll(BidsViewController.getColumns(bidList));
		this.bidList.setItems(this.retrievedBids);
	}
	
	public void setLotForBids(Lot lot) {
		try {
			this.lotForBids = lot;
			this.retrievedBids.clear();
			this.retrievedBids.addAll(getAuctionController().getVisibleBids(this.lotForBids.id));
		} catch (RequiresLoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@FXML protected void handlePlaceBidAction(ActionEvent event) {
		String moneyString = this.amountPounds.getText() + "." + amountPence.getText();
		
		try {
			BigDecimal amount = this.parseBigDecimal(moneyString);
			boolean privateBid = false;
			
			RadioButton selectedPrivacy = (RadioButton) this.bidPrivacyGroup.getSelectedToggle();
			switch(selectedPrivacy.getId()) {
				case "publicBid" :
					privateBid = false;
					break;
					
				case "privateBid" :
					privateBid = true;
					break;
					
				default :
					break;
			}
			
			try {
				getAuctionController().bidForLot(lotForBids.id, amount, privateBid);
				this.retrievedBids.clear();
				this.retrievedBids.addAll(getAuctionController().getVisibleBids(this.lotForBids.id));
			} catch (RequiresLoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnauthorisedBidException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidBidException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@FXML protected void handleAcceptHighestBidAction(ActionEvent event) {
		
	}
	
	private BigDecimal parseBigDecimal(String decimalString) throws ParseException  {
		System.out.println(decimalString);
		DecimalFormatSymbols symbols = new DecimalFormatSymbols();
		symbols.setDecimalSeparator('.');
		String pattern = "####.##";
		DecimalFormat decimalFormat = new DecimalFormat(pattern, symbols);
		decimalFormat.setParseBigDecimal(true);
		
		return (BigDecimal) decimalFormat.parse(decimalString);
	}
	
	public static ArrayList<TableColumn<Bid, ?>> getColumns(TableView<Bid> table) {
		ArrayList<TableColumn<Bid, ?>> columns = new ArrayList<>();
		
		TableColumn<Bid,String> bidTimeCol = new TableColumn<Bid,String>("Bid Time");
		bidTimeCol.setCellValueFactory(new Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty(param.getValue().bidTime.toString());
				
			}
		});
		
		TableColumn<Bid,String> bidderEmailCol = new TableColumn<Bid,String>("Bidder Email");
		bidderEmailCol.setCellValueFactory(new Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty(param.getValue().bidder.email);
				
			}
		 });
		
		TableColumn<Bid,String> bidAmountCol = new TableColumn<Bid,String>("Amount");
		bidAmountCol.setCellValueFactory(new Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty(param.getValue().amount.toString());
				
			}
		});
		
		TableColumn<Bid,String> bidTypeCol = new TableColumn<Bid,String>("Bid Type");
		bidTypeCol.setCellValueFactory(new Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty(param.getValue().privateBid ? "Private" : "Public");
			}
		});
		
		
		columns.add(bidTimeCol);
		columns.add(bidderEmailCol);
		columns.add(bidAmountCol);
		columns.add(bidTypeCol);
		
		return columns;
		
	}
}
