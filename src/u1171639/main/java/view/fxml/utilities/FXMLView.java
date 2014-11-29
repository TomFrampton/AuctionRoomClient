package u1171639.main.java.view.fxml.utilities;

import u1171639.main.java.view.fxml.controller.ViewController;
import javafx.scene.Parent;

public class FXMLView {
	private ViewController controller;
	private Parent component;
	
	public FXMLView() {
		
	}
	
	public FXMLView(ViewController controller, Parent component) {
		this.controller = controller;
		this.component = component;
	}
	
	public ViewController getController() {
		return this.controller;
	}
	
	public void setController(ViewController controller) {
		this.controller = controller;
	}
	
	public Parent getComponent() {
		return this.component;
	}
	
	public void setComponent(Parent component) {
		this.component = component;
	}
}
