package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.exception.ValidationException;
import u1171639.main.java.model.account.UserAccount;
import u1171639.main.java.utilities.Callback;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.control.Alert.AlertType;

public class LoginViewController extends ViewController {
	@FXML private TextField username;
	@FXML private PasswordField password;
	
	private Callback<Object, Void> loginSuccessCallback;
	
	@FXML protected void handleLoginButtonAction(ActionEvent event) {
		UserAccount credentials = new UserAccount(this.username.getText(), this.password.getText());
		
		try {
			getAuctionController().login(credentials);
			
			if(this.loginSuccessCallback != null) {
				this.loginSuccessCallback.call(new Object());
			}
		} catch (AuthenticationException e) {
			Alert alert = new Alert(AlertType.ERROR);
			alert.setTitle("Invalid Username or Password");
			alert.setHeaderText("Invalid Username or Password");
			alert.setContentText("The username and password combination you entered could not be verified.");
			alert.show();
		} catch (ValidationException e) {
			showValidationAlert(e.getViolations());
		}
	}

	public void setLoginSuccessCallback(Callback<Object, Void> loginSuccessCallback) {
		this.loginSuccessCallback = loginSuccessCallback;
	}

	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	
}