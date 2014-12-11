package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.Hashtable;
import java.util.ResourceBundle;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.layout.Pane;
import javafx.scene.text.Text;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.view.fxml.lot.display.LotInfoViewController;

public class ViewLotViewController extends ViewController {
	private Hashtable<String, LotInfoViewController> lotForms = new Hashtable<String, LotInfoViewController>();
	
	@FXML private Pane lotForm;
	
	private Lot lotToView;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		ObservableList<String> typeList = FXCollections.observableArrayList();
		
		for(Lot.Type type : Lot.Type.values()) {
			this.lotForms.put(type.toString(), (LotInfoViewController) this.loadView("lot/display/" + type.toString().toLowerCase() + ".fxml"));
			typeList.add(type.toString());
		}
	}
	
	public void setLotToView(Lot lot) {
		this.lotToView = lot;
		
		String lotType = lotToView.getClass().getSimpleName();
	
		LotInfoViewController lotFormController = this.lotForms.get(lotType);
		this.lotForm.getChildren().clear();
		this.lotForm.getChildren().add(lotFormController.getViewComponent());

		lotFormController.setLot(lot);
	}
	
	public Lot getLotToView() {
		return this.lotToView;
	}
}
