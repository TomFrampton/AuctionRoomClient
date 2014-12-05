package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ResourceBundle;

import u1171639.main.java.model.notification.Notification;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableView;
import javafx.scene.layout.Pane;

public class NotificationsViewController extends ViewController {
	@FXML private TableView<Notification> notificationList;
	@FXML private Pane notificationDetailPane;
	
	private Tab notificationTab;
	private int notificationCounter = 0;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		// TODO Auto-generated method stub
		
	}
	
	public void setNotificationTab(Tab notificationTab) {
		this.notificationTab = notificationTab;
	}
}
