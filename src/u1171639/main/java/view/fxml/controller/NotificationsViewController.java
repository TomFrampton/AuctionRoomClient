package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.notification.Notification;
import u1171639.main.java.utilities.Callback;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.layout.Pane;

public class NotificationsViewController extends ViewController {
	@FXML private TableView<Notification> notificationList;
	@FXML private Pane notificationDetailPane;
	
	private ObservableList<Notification> retrievedNotifications = FXCollections.observableArrayList();
	
	private Tab notificationTab;
	private int notificationCounter = 0;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		try {
			// Starting up so gather all the notifications for this user
			this.retrievedNotifications.addAll(getAuctionController().retrieveAllNotifications());
			
			// 
			
			//.. and register for future notifications
			getAuctionController().listenForNotifications(new Callback<Notification, Void>() {
				
				@Override
				public Void call(Notification notification) {
					retrievedNotifications.add(notification);
					return null;
				}
			});
			
		} catch (RequiresLoginException | AuctionCommunicationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	public void setNotificationTab(Tab notificationTab) {
		this.notificationTab = notificationTab;
	}
	
	public static ArrayList<TableColumn<Notification, ?>> getColumns(TableView<Notification> table) {
		ArrayList<TableColumn<Notification, ?>> columns = new ArrayList<>();
		
		TableColumn<Notification,String> notificationTimeCol = new TableColumn<Notification,String>("Time Received");
		notificationTimeCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Notification, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Notification, String> param) {
				return new SimpleStringProperty(param.getValue().timeReceived.toString());
				
			}
		});
		
		TableColumn<Notification,String> notificationTitleCol = new TableColumn<Notification,String>("Title");
		notificationTitleCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Notification, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Notification, String> param) {
				return new SimpleStringProperty(param.getValue().title);
				
			}
		 });
		
		TableColumn<Notification,String> notificationMessageCol = new TableColumn<Notification,String>("Message");
		notificationMessageCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<Notification, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<Notification, String> param) {
				return new SimpleStringProperty(param.getValue().message);
				
			}
		});
			
		columns.add(notificationTimeCol);
		columns.add(notificationTitleCol);
		columns.add(notificationMessageCol);
	
		return columns;
		
	}
}
