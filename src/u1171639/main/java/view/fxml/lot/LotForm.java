package u1171639.main.java.view.fxml.lot;

import java.util.List;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.controller.ViewController;

public abstract class LotForm extends ViewController {
	private Callback<Lot,Void> lotSubmittedCallback;

	public Callback<Lot, Void> getLotSubmittedCallback() {
		return lotSubmittedCallback;
	}

	public void setLotSubmittedCallback(Callback<Lot, Void> lotSubmittedCallback) {
		this.lotSubmittedCallback = lotSubmittedCallback;
	}
}
