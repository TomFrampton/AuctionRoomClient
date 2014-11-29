package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.exception.AuthenticationException;
import u1171639.main.java.model.account.User;
import u1171639.main.java.utilities.Callback;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.scene.text.Text;

public class LoginViewController extends ViewController {
	@FXML private TextField email;
	@FXML private PasswordField password;
	
	private Callback<Object, Void> loginSuccessCallback;
	
	@FXML protected void handleLoginButtonAction(ActionEvent event) {
		User credentials = new User(email.getText(), password.getText());
		
		try {
			this.getAuctionController().login(credentials);
			
			if(this.loginSuccessCallback != null) {
				this.loginSuccessCallback.call(new Object());
			}
		} catch (AuthenticationException e) {
			// TODO invalid login
			e.printStackTrace();
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