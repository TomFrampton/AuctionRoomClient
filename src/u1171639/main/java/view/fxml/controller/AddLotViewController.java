package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.lot.editor.LotFormViewController;

public class AddLotViewController extends ViewController {
	private Hashtable<String, LotFormViewController> lotForms = new Hashtable<String, LotFormViewController>();
	
	@FXML private ComboBox<String> lotTypeSelect;
	@FXML private Pane lotForm;
	
	private Callback<Lot,Void> lotAddedCallback;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> typeList = FXCollections.observableArrayList();
		
		for(Lot.Type type : Lot.Type.values()) {
			this.lotForms.put(type.toString(), (LotFormViewController) this.loadView("lot/editor/" + type.toString().toLowerCase() + ".fxml"));
			typeList.add(type.toString());
		}
		
		this.lotTypeSelect.setItems(typeList);
	}
	
	@FXML protected void handleLotTypeAction(ActionEvent event) {
		String selection = this.lotTypeSelect.getSelectionModel().getSelectedItem();
		if(selection != null) {
		
			final LotFormViewController controller = this.lotForms.get(selection);
			controller.setLotSubmittedCallback(new Callback<Lot, Void>() {
				
				@Override
				public Void call(Lot param) {
					try {
						param.id = getAuctionController().addLot(param, new Callback<Bid, Void>() {

							@Override
							public Void call(Bid bid) {
								Notification notification = new Notification();
								notification.title = "Bid Received!";
								notification.message = "A bid of £" + bid.amount.toString() + " was placed on '" +
										bid.lot.name + "'  by " + bid.bidder.username + ".";
								
								try {
									getAuctionController().addNotification(notification);
								} catch (RequiresLoginException | AuctionCommunicationException e) {
									// Something went wrong.
								}
								
								return null;
							}
						});
						
						lotTypeSelect.getSelectionModel().clearSelection();
						lotForm.getChildren().clear();
						
						controller.clearFields();
						
						lotAddedCallback.call(param);
						return null;
						
					} catch (RequiresLoginException | AuctionCommunicationException e) {
						System.out.println("Error addign lot");
						//MessageBox.show(getWindow(), e.toString(), "Error Adding Lot", MessageBox.ICON_ERROR | MessageBox.OK);
						return null;
					} catch (ValidationException e) {
						showValidationAlert(e.getViolations());
						return null;
					}
				}
			});
			
			this.lotForm.getChildren().add(this.lotForms.get(selection).getViewComponent());
		}
	}

	public void setLotAddedCallback(Callback<Lot, Void> lotAddedCallback) {
		this.lotAddedCallback = lotAddedCallback;
	}
}
