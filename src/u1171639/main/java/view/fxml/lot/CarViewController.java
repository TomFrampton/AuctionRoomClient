package u1171639.main.java.view.fxml.lot;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseEvent;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.view.fxml.controller.ViewController;

public class CarViewController extends LotForm {	
	@FXML private TextField name;
	@FXML private TextField description;
	@FXML private TextField make;
	@FXML private TextField model;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML protected void handleSubmitAction(ActionEvent event) {
		Car lot = new Car();
		
		lot.name = this.name.getText().equals("") ? null : this.name.getText();
		lot.description = this.description.getText().equals("") ? null : this.description.getText();
		lot.make = this.make.getText().equals("") ? null : this.make.getText();
		lot.model = this.model.getText().equals("") ? null : this.model.getText();
		
		if(getLotSubmittedCallback() != null) {
			getLotSubmittedCallback().call(lot);
		}
	}

}
