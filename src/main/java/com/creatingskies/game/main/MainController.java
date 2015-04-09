package com.creatingskies.game.main;

import java.io.IOException;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.HPos;
import javafx.geometry.VPos;
import javafx.scene.control.Label;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.config.company.CompanyController;
import com.creatingskies.game.config.event.GameEventTableViewController;
import com.creatingskies.game.config.obstacle.ObstaclesController;
import com.creatingskies.game.config.user.UsersController;
import com.creatingskies.game.config.weather.WeathersController;
import com.creatingskies.game.core.GameCoreController;
import com.creatingskies.game.editor.GameController;
import com.creatingskies.game.map.MapController;
import com.creatingskies.game.statistics.StatisticsController;

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
		
		createLauncher(0, "Users", "/images/rec_users.png", this::goToUsersPage);
		createLauncher(1, "Events", "/images/rec_events.png", this::goToEventsPage);
		createLauncher(2, "Games", "/images/rec_game.png", this::goToGamesPage);
		createLauncher(3, "Maps", "/images/rec_map.png", this::goToMaps);
		createLauncher(4, "Companies", "/images/rec_company.png", this::goToCompaniesPage);
		createLauncher(5, "Obstacles", "/images/rec_obstacle.png", this::goToObstaclesPage);
		createLauncher(6, "Weathers", "/images/rec_weather.png", this::goToWeathersPage);
		createLauncher(7, "Game Test", "/images/rec_test.png", this::goToTestPage);
		createLauncher(8, "Statistics", "/images/rec_statistics.png", this::goToStatisticsPage);
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
		
		box.getChildren().add(index, launcherPane);
	}
	
	private void goToUsersPage(MouseEvent event){
		new UsersController().show();
	}
	
	private void goToEventsPage(MouseEvent event){
		new GameEventTableViewController().show();
	}
	
	private void goToTestPage(MouseEvent event){
		new GameCoreController().show();
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
	
	private void goToStatisticsPage(MouseEvent event){
		new StatisticsController().show();
	}
	
}
