package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.exception.RegistrationException;
import u1171639.main.java.model.account.User;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class RegisterViewController extends ViewController {
	@FXML private TextField forename;
	@FXML private TextField surname;
	@FXML private TextField email;
	@FXML private PasswordField password;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	@FXML protected void handleRegisterButtonAction(ActionEvent event) {
		User newUser = new User();
		newUser.forename = this.forename.getText();
		newUser.surname = this.surname.getText();
		newUser.email = this.email.getText();
		newUser.password = this.password.getText();
		
		try {
			getAuctionController().register(newUser);
		} catch (RegistrationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}

}
