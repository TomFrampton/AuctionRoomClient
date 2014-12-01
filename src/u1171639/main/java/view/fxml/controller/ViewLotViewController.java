package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.lot.display.LotInfoViewController;
import u1171639.main.java.view.fxml.lot.editor.LotFormViewController;
import u1171639.main.java.view.fxml.utilities.FXMLView;

public class ViewLotViewController extends ViewController {
	private Hashtable<String, FXMLView> lotForms = new Hashtable<String, FXMLView>();
	
	@FXML private Pane lotForm;
	@FXML private Text lotName;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> typeList = FXCollections.observableArrayList();
		
		for(Lot.Type type : Lot.Type.values()) {
			this.lotForms.put(type.toString(), this.loadView("lot/display/" + type.toString().toLowerCase() + ".fxml"));
			typeList.add(type.toString());
		}
	}
	
	public void setLotToView(Lot lot) {
		this.lotName.setText(lot.name);
		
		String lotType = lot.getClass().getSimpleName();
		this.lotForm.getChildren().clear();
		this.lotForm.getChildren().add(this.lotForms.get(lotType).getComponent());
		final LotInfoViewController controller = (LotInfoViewController) this.lotForms.get(lotType).getController();
		controller.setLot(lot);
	}
}
