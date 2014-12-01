package u1171639.main.java.view.fxml.controller;

import java.io.IOException;

import u1171639.main.java.controller.AuctionController;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.Parent;
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
			Parent component = (Parent) fxmlLoader.load(ViewController.class.getClass().getResource("/u1171639/main/resources/" + fxmlResource).openStream());
			ViewController controller = (ViewController) fxmlLoader.getController();
			
			controller.setAuctionController(auctionController);
			controller.setViewComponent(component);
			
			return controller;
		} catch(IOException e) {
			// TODO
			e.printStackTrace();
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
}
