package com.creatingskies.game.main;

import java.io.IOException;

import com.creatingskies.game.model.HibernateSessionManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{
	
	private Stage primaryStage;
	private BorderPane mainLayout;
	
	public static void main(String[] args) {
		HibernateSessionManager.buildSessionFactory();
		launch(args);
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		this.primaryStage = primaryStage;
        this.primaryStage.setTitle("Game");
        
        initMainLayout();
        
        showLoginView();
	}
	
	public void initMainLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("MainLayout.fxml"));
            mainLayout = (BorderPane) loader.load();

            Scene scene = new Scene(mainLayout);
            
            primaryStage.setScene(scene);
            primaryStage.setFullScreen(true);
            primaryStage.show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
	
	public void showLoginView() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("../login/view/Login.fxml"));
            AnchorPane login = (AnchorPane) loader.load();
            
            mainLayout.setCenter(login);
            
            // Give the controller access to the main app.
//            PersonOverviewController controller = loader.getController();
//            controller.setMainApp(this);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
