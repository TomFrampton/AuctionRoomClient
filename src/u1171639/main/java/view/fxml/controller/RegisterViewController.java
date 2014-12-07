package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.model.account.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class RegisterViewController extends ViewController {
	@FXML private TextField forename;
	@FXML private TextField surname;
	@FXML private TextField email;
	@FXML private PasswordField password;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {

	}
	
	@FXML protected void handleRegisterButtonAction(ActionEvent event) {
		User newUser = new User();
		newUser.forename = this.forename.getText();
		newUser.surname = this.surname.getText();
		newUser.username = this.email.getText();
		newUser.password = this.password.getText();
		
		try {
			getAuctionController().register(newUser);
			
			Alert alert = new Alert(AlertType.CONFIRMATION);
			alert.setTitle("Registration Complete");
			alert.setHeaderText("Username Already in Use");
			alert.setContentText("Please choose another username. The one you entered is already being used by another user.");
			alert.show();
		} catch (RegistrationException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Username Already in Use");
			alert.setHeaderText("Username Already in Use");
			alert.setContentText("Please choose another username. The one you entered is already being used by another user.");
			alert.show();
		}
		
	}

}
