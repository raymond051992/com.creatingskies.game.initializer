package com.creatingskies.game.core;

import java.io.IOException;

import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class CloseGameDialogController {

	private Stage stage;
	private boolean isSaveButtonClicked = false;
	
	public boolean show(Stage gameStage) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("CloseGameDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("/css/dialog.css");
	        page.getStylesheets().add("/css/style.css");
	        page.getStyleClass().add("background");
			
	        stage = new Stage();
	        stage.setTitle("Game");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(gameStage);
	        stage.initStyle(StageStyle.UTILITY);
	        stage.setResizable(false);
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        CloseGameDialogController controller = loader.getController();
	        controller.setStage(stage);

	        stage.showAndWait();
	        
	        return isSaveButtonClicked;
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void handleOk(){
		isSaveButtonClicked = true;
		stage.close();
	}
	
	public void handleCancel(){
		isSaveButtonClicked = false;
		stage.close();
	}
}
