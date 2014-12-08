package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.lot.editor.LotFormViewController;

public class UpdateLotViewController extends ViewController {
	private Hashtable<String, LotFormViewController> lotForms = new Hashtable<String, LotFormViewController>();
	
	@FXML private ComboBox<String> lotTypeSelect;
	@FXML private Pane lotForm;
	
	private Callback<Lot,Void> lotUpdatedCallback;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> typeList = FXCollections.observableArrayList();
		
		for(Lot.Type type : Lot.Type.values()) {
			
			LotFormViewController lotFormController = (LotFormViewController) this.loadView("lot/editor/" + type.toString().toLowerCase() + ".fxml");
			
			this.lotForms.put(type.toString(), lotFormController);
			typeList.add(type.toString());
			
			lotFormController.setLotSubmittedCallback(new Callback<Lot, Void>() {
				
				@Override
				public Void call(Lot param) {
					try {
						getAuctionController().updateLot(param);
						lotUpdatedCallback.call(param);
						return null;
					} catch (RequiresLoginException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (AuctionCommunicationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						return null;
					} catch (ValidationException e) {
						showValidationAlert(e.getViolations());
						return null;
					}
				}
			});
		}
		
		this.lotTypeSelect.setItems(typeList);
	}

	public void setLotUpdatedCallback(Callback<Lot, Void> lotUpdatedCallback) {
		this.lotUpdatedCallback = lotUpdatedCallback;
	}
	
	public void setLotToUpdate(Lot lot) {
		String lotType = lot.getClass().getSimpleName();
		
		this.lotTypeSelect.getSelectionModel().select(lotType);
		
		LotFormViewController lotFormController = this.lotForms.get(lotType);
		
		this.lotForm.getChildren().clear();
		this.lotForm.getChildren().add(lotFormController.getViewComponent());

		lotFormController.setLot(lot);
	}
}
