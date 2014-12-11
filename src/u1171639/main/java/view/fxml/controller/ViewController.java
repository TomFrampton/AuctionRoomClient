package u1171639.main.java.view.fxml.controller;

import java.io.IOException;
import java.util.List;

import net.sf.oval.ConstraintViolation;
import u1171639.main.java.controller.AuctionController;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.stage.Stage;
import javafx.util.Callback;

public abstract class ViewController implements Initializable {
	private AuctionController auctionController;
	private Parent viewComponent;

	public ViewController() {
		
	}
	
	public static ViewController loadView(String fxmlResource, final AuctionController auctionController) {
		FXMLLoader fxmlLoader = new FXMLLoader();
		
		 fxmlLoader.setControllerFactory(new Callback<Class<?>, Object>() {
			@Override
			public Object call(Class<?> param) {
				try {
					ViewController controller = (ViewController) param.newInstance();
					controller.setAuctionController(auctionController);
					return controller;
				} catch (InstantiationException | IllegalAccessException e) {
					return null;
				}
			}
		});
		
		try {
			Parent component = (Parent) fxmlLoader.load(ViewController.class.getClass().getResource("/u1171639/main/resources/fxml/" + fxmlResource).openStream());
			ViewController controller = (ViewController) fxmlLoader.getController();
			
			controller.setAuctionController(auctionController);
			controller.setViewComponent(component);
			
			return controller;
		} catch(IOException e) {
			// TODO
			e.printStackTrace();
			return null;
		}
	}
	
	public ViewController loadView(String fxmlResource) {
		return ViewController.loadView(fxmlResource, this.auctionController);
	}
	
	public AuctionController getAuctionController() {
		return this.auctionController;
	}

	public void setAuctionController(AuctionController auctionController) {
		this.auctionController = auctionController;
	}
	
	public Parent getViewComponent() {
		return this.viewComponent;
	}
	
	public void setViewComponent(Parent viewComponent) {
		this.viewComponent = viewComponent;
	}
	
	public Stage getWindow() {
		return (Stage) this.viewComponent.getScene().getWindow();
	}
	
	public void showValidationAlert(List<ConstraintViolation> violations) {
		String errors = "";
		
		for(ConstraintViolation violation : violations) {
			String valMsg = violation.getMessage();
			int i = valMsg.indexOf(' ');
			
			String fullFieldName = valMsg.substring(0, i);
			String shortFieldName = fullFieldName.substring(fullFieldName.lastIndexOf('.') + 1);
			
			shortFieldName = shortFieldName.substring(0,1).toUpperCase() + shortFieldName.substring(1);
			
			errors += shortFieldName + valMsg.substring(i) + "\n";
		}
		
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Validation Errors");
		alert.setHeaderText("Validation Errors");
		alert.setContentText(errors);
		
		alert.show();
		alert.setHeight(150 + ( 25 * violations.size()));
	}
}
