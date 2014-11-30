package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import u1171639.main.java.model.lot.Lot;

public class LotViewController extends ViewController {
	private Lot lot;
	
	@FXML private Text lotName;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void setLot(Lot lot) {
		this.lot = lot;
	}

}
