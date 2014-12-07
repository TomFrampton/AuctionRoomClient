package u1171639.main.java.view.fxml.lot.display;

import java.net.URL;
import java.util.ResourceBundle;

import javafx.fxml.FXML;
import javafx.scene.text.Text;
import u1171639.main.java.model.lot.Car;
import u1171639.main.java.model.lot.Lot;

public class CarInfoViewController extends LotInfoViewController {
	@FXML private Text lotName;
	@FXML private Text lotType;
	@FXML private Text description;
	@FXML private Text make;
	@FXML private Text model;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.lotType.setText("Car");
	}
	
	@Override
	public void setLot(Lot lot) {
		Car car = (Car) lot;
		this.lotName.setText(car.name);
		this.description.setText(car.description);
		this.make.setText(car.make);
		this.model.setText(car.model);
	}

}
