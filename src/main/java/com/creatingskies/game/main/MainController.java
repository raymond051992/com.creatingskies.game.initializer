package com.creatingskies.game.main;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.GameTestController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.config.company.CompanyController;
import com.creatingskies.game.config.obstacle.ObstaclesController;
import com.creatingskies.game.config.user.UsersController;
import com.creatingskies.game.config.weather.WeathersController;
import com.creatingskies.game.editor.GameController;
import com.creatingskies.game.map.MapController;

public class MainController extends ViewController{

	@FXML FlowPane box;
	
	@Override
	protected String getViewTitle() {
		return null;
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Main.fxml"));
            AnchorPane login = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(login);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@FXML
	public void initialize() {
		super.init();
		
		createLauncher(0, "Users", "/images/ic_account_child_128.png", this::goToUsersPage);
		createLauncher(1, "Events", "/images/ic_event_128.png", this::goToEventsPage);
		createLauncher(2, "Games", "/images/ic_desktop_windows_128.png", this::goToGamesPage);
		createLauncher(3, "Maps", "/images/ic_swap_calls_128.png", this::goToMaps);
		createLauncher(4, "Companies", "/images/ic_company_128.png", this::goToCompaniesPage);
		createLauncher(5, "Obstacles", "/images/ic_obstacle_128.png", this::goToObstaclesPage);
		createLauncher(6, "Weathers", "/images/ic_weather_128.png", this::goToWeathersPage);
		createLauncher(7, "Game Test", "/images/ic_test_128.png", this::goToTestPage);
	}
	
	private void createLauncher(Integer index, String name, String imagePath,
			EventHandler<? super MouseEvent> mouseClickHandler){
		GridPane launcherPane = new GridPane();
		ImageView launcherIcon = new ImageView(imagePath);
		Label launcherName = new Label(name);
		
		launcherPane.setOnMouseClicked(mouseClickHandler);
		launcherPane.getStyleClass().add("launcher-pane");
		
		GridPane.setHalignment(launcherName, HPos.CENTER);
		GridPane.setValignment(launcherName, VPos.CENTER);
		
		GridPane.setHalignment(launcherIcon, HPos.CENTER);
		GridPane.setValignment(launcherIcon, VPos.CENTER);
		
		launcherPane.addRow(0, launcherIcon);
		launcherPane.addRow(1, launcherName);
		
		
		box.getChildren().add(launcherPane);
	}
	
	private void goToUsersPage(MouseEvent event){
		new UsersController().show();
	}
	
	private void goToEventsPage(MouseEvent event){
		new AlertDialog(AlertType.INFORMATION,"Events","Events",null).showAndWait();
	}
	
	private void goToTestPage(MouseEvent event){
		new GameTestController().show();
	}
	
	private void goToGamesPage(MouseEvent event){
		new GameController().show();
	}
	
	private void goToMaps(MouseEvent event){
		new MapController().show();
	}
	
	private void goToCompaniesPage(MouseEvent event){
		new CompanyController().show();
	}
	
	private void goToObstaclesPage(MouseEvent event){
		new ObstaclesController().show();
	}
	
	private void goToWeathersPage(MouseEvent event){
		new WeathersController().show();
	}
	
}
