package u1171639.main.java.view.fxml.controller;

import java.io.IOException;
import java.util.List;

import net.sf.oval.ConstraintViolation;
import u1171639.main.java.controller.AuctionController;
import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.BidNotFoundException;
import u1171639.main.java.exception.InvalidBidException;
import u1171639.main.java.exception.LotNotFoundException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedBidException;
import u1171639.main.java.exception.UnauthorisedLotActionException;
import u1171639.main.java.exception.UnauthorisedNotificationActionException;
import u1171639.main.java.exception.UserNotFoundException;
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
			System.err.println("Error: " + e.getMessage());
			System.exit(1);
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
	
	public void showErrorAlert(AuctionCommunicationException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Error Communicating With Server");
		alert.setHeaderText("Error Communicating With Server");
		alert.setContentText("There was an error when communicating with the auction server. Please try again.");
		alert.show();
	}
	
	public void showErrorAlert(RequiresLoginException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Login Required");
		alert.setHeaderText("Login Required");
		alert.setContentText("You must log in to partake in the auction.");
		alert.show();
	}
	
	public void showErrorAlert(InvalidBidException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Invalid Bid Amount");
		alert.setHeaderText("Invalid Bid Amount");
		alert.setContentText("You must make a bid greater than the currrent highest bid.");
		alert.show();
	}
	
	public void showErrorAlert(LotNotFoundException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Lot Not Found");
		alert.setHeaderText("Lot Not Found");
		alert.setContentText("That Lot was not found in the auction. It may have ended or been withdrawn by the seller.");
		alert.show();
	}
	
	public void showErrorAlert(BidNotFoundException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Bid Not Found");
		alert.setHeaderText("Bid Not Found");
		alert.setContentText("We could not find that bid in the auction.");
		alert.show();
	}
	
	public void showErrorAlert(UnauthorisedLotActionException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Unauthorised Lot Action");
		alert.setHeaderText("Unauthorised Lot Action");
		alert.setContentText("Only the seller of the Lot is authorised to do that.");
		alert.show();
	}
	
	public void showErrorAlert(NotificationNotFoundException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Notification Not Found");
		alert.setHeaderText("Notification Not Found");
		alert.setContentText("We could not find that notification on our server.");
		alert.show();
	}
	
	public void showErrorAlert(UnauthorisedNotificationActionException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Unauthorised Notification Action");
		alert.setHeaderText("Unauthorised Notification Action");
		alert.setContentText("Only the receipient of the notification is allowed to mark it as read.");
		alert.show();
	}
	
	public void showErrorAlert(UserNotFoundException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("User Not Found");
		alert.setHeaderText("User Not Found");
		alert.setContentText("We could not find that user in our database.");
		alert.show();
	}
	
	public void showErrorAlert(UnauthorisedBidException e) {
		Alert alert = new Alert(AlertType.ERROR);
		alert.setTitle("Invalid Bid");
		alert.setHeaderText("Invalid Bid");
		alert.setContentText("You cannot bid on your own Lot.");
		alert.show();
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
		alert.setHeight(175 + ( 25 * violations.size()));
	}
}
