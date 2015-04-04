package com.creatingskies.game.main;

import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.layout.AnchorPane;

import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.config.user.UsersController;

public class MainController {

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
	private void goToUsersPage(){
		new UsersController().show();
	}
	
	@FXML
	private void goToEventsPage(){
		new AlertDialog(AlertType.INFORMATION,"Events","Events",null).showAndWait();
	}
	
	@FXML
	private void goToGamesPage(){
		new AlertDialog(AlertType.INFORMATION,"Games","Games",null).showAndWait();
	}
	
	@FXML
	private void goToStatisticsPage(){
		new AlertDialog(AlertType.INFORMATION,"Statistics","Statistics",null).showAndWait();
	}
}
