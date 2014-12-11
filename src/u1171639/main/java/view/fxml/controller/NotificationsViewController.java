package u1171639.main.java.view.fxml.controller;

import java.net.URL;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ResourceBundle;
import java.util.Set;

import u1171639.main.java.exception.AuctionCommunicationException;
import u1171639.main.java.exception.NotificationNotFoundException;
import u1171639.main.java.exception.RequiresLoginException;
import u1171639.main.java.exception.UnauthorisedNotificationActionException;
import u1171639.main.java.model.lot.Bid;
import u1171639.main.java.model.lot.Lot;
import u1171639.main.java.model.notification.UserNotification;
import u1171639.main.java.utilities.Callback;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.value.ObservableValue;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TableColumn.CellDataFeatures;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.Pane;

public class NotificationsViewController extends ViewController {
	@FXML private TableView<UserNotification> notificationList;
	@FXML private Pane notificationDetailPane;
	
	private ObservableList<UserNotification> retrievedNotifications = FXCollections.observableArrayList();
	
	private Tab notificationTab;
	
	@Override
	public void initialize(URL location, ResourceBundle resources) {
		this.notificationList.getColumns().addAll(NotificationsViewController.getColumns(this.notificationList));
		this.notificationList.setItems(this.retrievedNotifications);
		this.notificationList.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
		
		try {
			// Starting up so gather all the notifications for this user
			this.retrievedNotifications.addAll(getAuctionController().retrieveAllNotifications());
			
			//.. and register for future notifications
			getAuctionController().listenForNotifications(new Callback<UserNotification, Void>() {
				
				@Override
				public Void call(UserNotification notification) {
					retrievedNotifications.add(notification);
					setTabText();
					return null;
				}
			});
			
		} catch (RequiresLoginException e) {
			showErrorAlert(e);
		} catch(AuctionCommunicationException e) {
			showErrorAlert(e);
		}
	}
	
	@FXML protected void handleNotificationClicked(MouseEvent event) {
		if(this.notificationList.getSelectionModel().getSelectedIndex() >= 0) {
			UserNotification notification = this.notificationList.getSelectionModel().getSelectedItem();
			
			if(!notification.read) {
				// Mark notification as read
				try {
					getAuctionController().markNotificationRead(notification.id);
					notification.read = true;
					
					this.setTabText();
					
				} catch (RequiresLoginExceptione e) {
					
				}
			}
		}
	}
	
	public void setNotificationTab(Tab notificationTab) {
		this.notificationTab = notificationTab;
		this.setTabText();
	}
	
	public static ArrayList<TableColumn<UserNotification, ?>> getColumns(final TableView<UserNotification> table) {
		ArrayList<TableColumn<UserNotification, ?>> columns = new ArrayList<>();
		
		TableColumn<UserNotification,String> notificationTimeCol = new TableColumn<UserNotification,String>("Time Received");
		notificationTimeCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<UserNotification, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<UserNotification, String> param) {
				return new SimpleStringProperty(param.getValue().timeReceived.toString());
				
			}
		});
		
		TableColumn<UserNotification,String> notificationTitleCol = new TableColumn<UserNotification,String>("Title");
		notificationTitleCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<UserNotification, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<UserNotification, String> param) {
				return new SimpleStringProperty(param.getValue().title);
				
			}
		 });
		
		TableColumn<UserNotification,String> notificationMessageCol = new TableColumn<UserNotification,String>("Message");
		notificationMessageCol.setCellValueFactory(new javafx.util.Callback<CellDataFeatures<UserNotification, String>, ObservableValue<String>>() {

			@Override
			public ObservableValue<String> call(CellDataFeatures<UserNotification, String> param) {
				return new SimpleStringProperty(param.getValue().message);
				
			}
		});	
		
		table.setRowFactory(new javafx.util.Callback<TableView<UserNotification>, TableRow<UserNotification>>() {
			
			@Override
			public TableRow<UserNotification> call(TableView<UserNotification> param) {
				final TableRow<UserNotification> row = new TableRow<UserNotification>() {
					@Override
					public void updateItem(UserNotification item, boolean empty) {
						super.updateItem(item, empty);
						if(item != null) {
							if(!item.read) {
								if(!this.getStyleClass().contains("unread-notification")) {
									this.getStyleClass().add("unread-notification");
								}
							}
						}
					}
				};
				
				row.setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						row.getStyleClass().remove("unread-notification");
					}
					
				});
				
				return row;
			}
		});
			
		columns.add(notificationTimeCol);
		columns.add(notificationTitleCol);
		columns.add(notificationMessageCol);
	
		return columns;
		
	}
	
	private void setTabText() {
		int notificationCounter = 0;
		
		for(UserNotification notification : this.retrievedNotifications) {
			if(!notification.read) {
				notificationCounter++;
			}
		}
		
		final int counter = notificationCounter;
		
		Platform.runLater(new Runnable() {
			@Override
			public void run() {
				notificationTab.setText("Notifications (" + counter + ")");
			}
		});
	}
}
