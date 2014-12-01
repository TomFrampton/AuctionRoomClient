package u1171639.main.java.view.fxml.lot.editor;

import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.utilities.Callback;
import u1171639.main.java.view.fxml.controller.ViewController;

public abstract class LotFormViewController extends ViewController {
	private Callback<Lot,Void> lotSubmittedCallback;

	public Callback<Lot, Void> getLotSubmittedCallback() {
		return this.lotSubmittedCallback;
	}

	public void setLotSubmittedCallback(Callback<Lot, Void> lotSubmittedCallback) {
		this.lotSubmittedCallback = lotSubmittedCallback;
	}
	
	public abstract void clearFields();
	public abstract void setLot(Lot lot);
}
