package u1171639.main.java.view.fxml.controller;

import java.math.BigDecimal;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.ResourceBundle;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.BidNotFoundException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.UserNotification;
import u1171639.main.java.utilities.Callback;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Parent;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TableColumn;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.control.Alert;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.ToggleGroup;
import javafx.scene.layout.Pane;

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
	
	private Callback<Lot, Void> lotWithdrawnCallback;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.bidList.getColumns().addAll(BidsViewController.getColumns(this.bidList));
		this.bidList.setItems(this.retrievedBids);
		this.bidList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
	}
	
	public void setLotForBids(Lot lot) {
		try {
			this.lotForBids = lot;
			this.retrievedBids.clear();
			this.retrievedBids.addAll(getAuctionController().getVisibleBids(this.lotForBids.id));
		} catch (RequiresLoginException | LotNotFoundException | AuctionCommunicationException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Lot Not Found");
			alert.setHeaderText("Lot Not Found");
			alert.setContentText("That Lot was not found. It may have been removed.");
			alert.show();
		}
	}
	
	@FXML protected void handlePlaceBidAction(ActionEvent event) {
		String moneyString = this.amountPounds.getText() + "." + this.amountPence.getText();
		
		try {
			BigDecimal amount = parseBigDecimal(moneyString);
			Boolean privateBid = null;
			
			RadioButton selectedPrivacy = (RadioButton) this.bidPrivacyGroup.getSelectedToggle();
			
			if(selectedPrivacy != null) {
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
			}
			
			try {
				// Make a bid for the lot and subscribe to lot updates
				getAuctionController().bidForLot(new Bid(this.lotForBids.id, amount, privateBid));
				getAuctionController().listenForLotUpdates(this.lotForBids.id, new Callback<Lot, Void>() {

					@Override
					public Void call(Lot lot) {
						UserNotification notification = new UserNotification();
						notification.title = "Lot Updated!";
						notification.message = "Lot '" + lot.name + "' has been updated.";
						
						try {
							getAuctionController().addNotification(notification);
						} catch (RequiresLoginException | AuctionCommunicationException e) {
							// Something went wrong.
						}
						return null;
					}
				});
				
				// Also listen for bids on this lot
				getAuctionController().listenForBidsOnLot(this.lotForBids.id, new Callback<Bid, Void>() {

					@Override
					public Void call(Bid bid) {
						if(!bid.bidderId.equals(getAuctionController().getCurrentUser().id)) {
							UserNotification notification = new UserNotification();
							notification.title = "Bid Placed!";
							notification.message = "A bid of �" + bid.amount.toString() + " was placed on '" +
									bid.lot.name + "'  at " + bid.bidTime.toString() + ".";
							
							try {
								getAuctionController().addNotification(notification);
							} catch (RequiresLoginException | AuctionCommunicationException e) {
								// Something went wrong.
							}
						}
						return null;
					}
				});
				
				// Listen for the accepted bid
				getAuctionController().listenForAcceptedBidOnLot(this.lotForBids.id, new Callback<Bid, Void>() {

					@Override
					public Void call(final Bid bid) {
						Platform.runLater(new Runnable() {
							
							@Override
							public void run() {
								UserNotification notification = new UserNotification();
								// Test if the accepted bid was on one of our lots.
								if(bid.bidderId.equals(getAuctionController().getCurrentUser().id)) {
									notification.title = "Lot Won!";
									notification.message = "The bid of �" + bid.amount.toString() + " that you placed on '" + bid.lot.name + "' at " +
											bid.bidTime.toString() + " has been accepted!";
								} else {
									notification.title = "Lot Not Won";
									notification.message = "The lot '" + bid.lot.name + "' has ended with a winning bid of " + bid.amount.toString() + ".";
								}
								
								try {
									getAuctionController().addNotification(notification);
								} catch (RequiresLoginException e) {
									showErrorAlert(e);
								} catch (AuctionCommunicationException e) {
									showErrorAlert(e);
								}
							}
						});
						
						return null;
					}
				});
				
				// And listen for the removal of the lot
				getAuctionController().listenForLotRemoval(this.lotForBids.id, new Callback<Lot, Void>() {

					@Override
					public Void call(Lot removedLot) {
						UserNotification notification = new UserNotification();
						notification.title = "Lot Removed";
						notification.message =  "The lot '" + removedLot.name + "' has been removed from the auction by the seller.";
						
						try {
							getAuctionController().addNotification(notification);
						} catch (RequiresLoginException e) {
							showErrorAlert(e);
						} catch (AuctionCommunicationException e) {
							showErrorAlert(e);
						}
						
						return null;
					}
				});
				
				this.retrievedBids.clear();
				this.retrievedBids.addAll(getAuctionController().getVisibleBids(this.lotForBids.id));
			} catch (RequiresLoginException e) {
				showErrorAlert(e);
			} catch (UnauthorisedBidException e) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setTitle("Invalid Bid");
				alert.setHeaderText("Invalid Bid");
				alert.setContentText("You cannot bid on your own Lot.");
				alert.show();
			} catch(InvalidBidException e) {
				showErrorAlert(e);
			} catch (AuctionCommunicationException e) {
				showErrorAlert(e);
			} catch (LotNotFoundException e) {
				showErrorAlert(e);
			} catch (ValidationException e) {
				showValidationAlert(e.getViolations());
			}
		} catch (ParseException e1) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Invalid Number Format");
			alert.setHeaderText("Invalid Number Format");
			alert.setContentText("The amount you entered was not in correct number format.");
			alert.show();
		}
	}
	
	@FXML protected void handleAcceptBidAction(ActionEvent event) {
		if(this.bidList.getSelectionModel().getSelectedIndex() >= 0) {
			Bid selected = this.bidList.getSelectionModel().getSelectedItem();
			
			try {
				getAuctionController().acceptBid(selected.id);
				
				Alert alert = new Alert(AlertType.INFORMATION);
				alert.setTitle("Bid Accepted");
				alert.setHeaderText("Bid Accepted");
				alert.setContentText("The bid has been accepted and " + selected.bidder + " has been informed.");
				alert.show();
				
			} catch (RequiresLoginException e) {
				showErrorAlert(e);
			} catch(BidNotFoundException e) {
				showErrorAlert(e);
			} catch(AuctionCommunicationException e) {
				showErrorAlert(e);
			}
			
		} else {
			Alert alert = new Alert(AlertType.WARNING);
			alert.setTitle("Select a Bid");
			alert.setHeaderText("Select a Bid");
			alert.setContentText("You must select a bid to accept.");
			alert.show();
		}
	}
	
	@FXML protected void handleWithdrawLotAction(ActionEvent event) {
		try {
			getAuctionController().removeLot(this.lotForBids.id);
			
			if(this.lotWithdrawnCallback != null) {
				this.lotWithdrawnCallback.call(this.lotForBids);
			}
		} catch (UnauthorisedLotActionException e) {
			showErrorAlert(e);
		} catch (LotNotFoundException e) {
			showErrorAlert(e);
		} catch (RequiresLoginException e) {
			showErrorAlert(e);
		} catch (AuctionCommunicationException e) {
			showErrorAlert(e);
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
	
	public void setLotWithdrawnCallback(Callback<Lot, Void> lotWithdrawnCallback) {
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
		
		TableColumn<Bid,String> bidderCol = new TableColumn<Bid,String>("Bidder");
		bidderCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Bid, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Bid, String> param) {
				return new SimpleStringProperty(param.getValue().bidder.username);
				
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
		columns.add(bidderCol);
		columns.add(bidAmountCol);
		columns.add(bidTypeCol);
		
		return columns;
		
	}
}
