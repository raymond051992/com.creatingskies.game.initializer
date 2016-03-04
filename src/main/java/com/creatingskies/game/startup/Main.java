package com.creatingskies.game.startup;

import java.io.IOException;

import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.resources.GameResourcesManager;
import com.creatingskies.game.login.LoginController;
import com.creatingskies.game.model.HibernateSessionManager;

import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.stage.Stage;

public class Main extends Application{
	
	public static void main(String[] args) {
		launch(args);
	}
	
	@Override
	public void init() throws Exception {
		super.init();
		HibernateSessionManager.buildSessionFactory();
	}
	
	@Override
	public void stop() throws Exception {
		HibernateSessionManager.shutdown();
		super.stop();
	}
	
	@Override
	public void start(Stage primaryStage) throws Exception {
		GameResourcesManager.removeTmpFiles();
		
		MainLayout.setPrimaryStage(primaryStage);
		MainLayout.getPrimaryStage().setTitle("Game");
        initMainLayout();
        
        new LoginController().show();
	}
	
	public void initMainLayout() {
        try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(Main.class.getResource("MainLayout.fxml"));
            MainLayout.setRootLayout((BorderPane) loader.load());
            
            Scene scene = new Scene(MainLayout.getRootLayout());
            
            MainLayout.getPrimaryStage().setScene(scene);
            MainLayout.getPrimaryStage().setFullScreen(true);
            MainLayout.getPrimaryStage().show();
            
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
