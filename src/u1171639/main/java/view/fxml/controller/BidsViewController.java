package u1171639.main.java.view.fxml.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;
//import jfx.messagebox.MessageBox;
import jfx.messagebox.MessageBox;

public class BidsViewController extends ViewController {
	@FXML private Parent buyerOptions;
	@FXML private Parent sellerOptions;
	
	@FXML private Pane optionsView;
	
	@FXML private TableView<Bid> bidList;
	
	@FXML private TextField amountPounds;
	@FXML private TextField amountPence;
	@FXML private ToggleGroup bidPrivacyGroup;
	
	private Lot lotForBids;
	
	private ObservableList<Bid> retrievedBids = FXCollections.observableArrayList();
	
	private Callback<Void, Void> lotWithdrawnCallback;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bidList.getColumns().addAll(BidsViewController.getColumns(this.bidList));
		this.bidList.setItems(this.retrievedBids);
	}
	
	public void setLotForBids(Lot lot) {
		try {
			this.lotForBids = lot;
			this.retrievedBids.clear();
			this.retrievedBids.addAll(getAuctionController().getVisibleBids(this.lotForBids.id));
		} catch (RequiresLoginException | LotNotFoundException | AuctionCommunicationException e) {
			MessageBox.show(getWindow(), e.toString(), "Error Loading Lot", MessageBox.ICON_ERROR | MessageBox.OK);
		}
	}
	
	@FXML protected void handlePlaceBidAction(ActionEvent event) {
		String moneyString = this.amountPounds.getText() + "." + this.amountPence.getText();
		
		try {
			BigDecimal amount = parseBigDecimal(moneyString);
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
				getAuctionController().bidForLot(new Bid(this.lotForBids.id, amount, privateBid));
				this.retrievedBids.clear();
				this.retrievedBids.addAll(getAuctionController().getVisibleBids(this.lotForBids.id));
			} catch (RequiresLoginException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (UnauthorisedBidException | InvalidBidException | LotNotFoundException e) {
				//MessageBox.show(getWindow(), e.toString(), 
						//"Error Placing Bid", MessageBox.ICON_ERROR | MessageBox.OK);
			} catch (AuctionCommunicationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (ParseException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
	}
	
	@FXML protected void handleAcceptBidAction(ActionEvent event) {
		
	}
	
	@FXML protected void handleWithdrawLotAction(ActionEvent event) {
		try {
			getAuctionController().removeLot(this.lotForBids.id);
			
			if(this.lotWithdrawnCallback != null) {
				this.lotWithdrawnCallback.call(null);
			}
		} catch (UnauthorisedLotActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (LotNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (RequiresLoginException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AuctionCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void showBuyingFields() {
		this.optionsView.getChildren().clear();
		this.optionsView.getChildren().add(this.buyerOptions);
	}
	
	public void showSellingFields() {
		this.optionsView.getChildren().clear();
		this.optionsView.getChildren().add(this.sellerOptions);
	}
	
	public void setLotWithdrawnCallback(Callback<Void, Void> lotWithdrawnCallback) {
		this.lotWithdrawnCallback = lotWithdrawnCallback;
	}
	
	private BigDecimal parseBigDecimal(String decimalString) throws ParseException  {
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
		bidTimeCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty(param.getValue().bidTime.toString());
				
			}
		});
		
		TableColumn<Bid,String> bidderEmailCol = new TableColumn<Bid,String>("Bidder Email");
		bidderEmailCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty(param.getValue().bidder.email);
				
			}
		 });
		
		TableColumn<Bid,String> bidAmountCol = new TableColumn<Bid,String>("Amount");
		bidAmountCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty("�" + param.getValue().amount.toString());
				
			}
		});
		
		TableColumn<Bid,String> bidTypeCol = new TableColumn<Bid,String>("Bid Type");
		bidTypeCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

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
