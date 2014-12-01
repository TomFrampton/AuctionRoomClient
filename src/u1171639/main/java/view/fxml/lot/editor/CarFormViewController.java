package u1171639.main.java.view.fxml.lot.editor;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;

public class CarFormViewController extends LotFormViewController {	
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
	
	@Override
	public void clearFields() {
		this.name.setText("");
		this.description.setText("");
		this.make.setText("");
		this.model.setText("");
	}
	
	@Override
	public void setLot(Lot lot) {
		Car car = (Car) lot;
		this.name.setText(car.name);
		this.description.setText(car.description);
		this.make.setText(car.make);
		this.model.setText(car.model);
	}

}
