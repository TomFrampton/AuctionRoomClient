package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.model.account.UserAccount;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;
import javafx.scene.layout.BorderPane;
import javafx.scene.text.Text;

public class AccountViewController extends ViewController {
	@FXML private BorderPane accountPane;
	
	@FXML private TextField forename;
	@FXML private TextField surname;
	@FXML private Text username;
	
	private UserAccount user;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.user = getAuctionController().getCurrentUser();
		
		this.forename.setText(this.user.forename);
		this.surname.setText(this.user.surname);
		this.username.setText(this.user.username);
	}
}
