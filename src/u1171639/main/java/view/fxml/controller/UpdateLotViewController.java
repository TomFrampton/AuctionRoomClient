package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.Hashtable;
import java.util.List;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Pane;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.lot.editor.LotFormViewController;
import u1171639.main.java.view.fxml.utilities.FXMLView;

public class UpdateLotViewController extends ViewController {
	private Hashtable<String, FXMLView> lotForms = new Hashtable<String, FXMLView>();
	
	@FXML private ComboBox<String> lotTypeSelect;
	@FXML private Pane lotForm;
	
	private Callback<Lot,Void> lotSubmittedCallback;
	
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> typeList = FXCollections.observableArrayList();
		
		for(Lot.Type type : Lot.Type.values()) {
			this.lotForms.put(type.toString(), this.loadView("lot/editor/" + type.toString().toLowerCase() + ".fxml"));
			typeList.add(type.toString());
		}
		
		this.lotTypeSelect.setItems(typeList);
	}

	public void setLotSubmittedCallback(Callback<Lot, Void> lotSubmittedCallback) {
		this.lotSubmittedCallback = lotSubmittedCallback;
	}
	
	public void setLotToUpdate(Lot lot) {
		String lotType = lot.getClass().getSimpleName();
		this.lotTypeSelect.getSelectionModel().select(lotType);
		this.lotForm.getChildren().clear();
		this.lotForm.getChildren().add(this.lotForms.get(lotType).getComponent());
		final LotFormViewController controller = (LotFormViewController) this.lotForms.get(lotType).getController();
		controller.setLot(lot);
	}
}
