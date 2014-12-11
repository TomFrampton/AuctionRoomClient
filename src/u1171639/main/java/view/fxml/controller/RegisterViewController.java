package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.account.UserAccount;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class RegisterViewController extends ViewController {
	@FXML private TextField forename;
	@FXML private TextField surname;
	@FXML private TextField username;
	@FXML private PasswordField password;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	
	@FXML protected void handleRegisterButtonAction(ActionEvent event) {
		UserAccount newUser = new UserAccount();
		newUser.forename = this.forename.getText();
		newUser.surname = this.surname.getText();
		newUser.username = this.username.getText();
		newUser.password = this.password.getText();
		
		try {
			getAuctionController().register(newUser);
			
			Alert alert = new Alert(AlertType.INFORMATION);
			alert.setTitle("Registration Complete");
			alert.setHeaderText("Registration Complete");
			alert.setContentText("You can now log in using your username and password.");
			alert.show();
		} catch (RegistrationException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Username Already in Use");
			alert.setHeaderText("Username Already in Use");
			alert.setContentText("Please choose another username. The one you entered is already being used by another user.");
			alert.show();
		} catch (ValidationException e) {
			showValidationAlert(e.getViolations());
		} catch (AuctionCommunicationException e) {
			showErrorAlert(e);
		}
		
	}

}
