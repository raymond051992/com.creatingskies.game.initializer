package com.creatingskies.game.config.obstacle;

import java.io.IOException;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.core.Tile;
import com.creatingskies.game.model.obstacle.Obstacle;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ObstacleDialogController extends ViewController {

	@FXML private TextField nameField;
	@FXML private CheckBox forRowingCheckBox;
	@FXML private CheckBox forCyclingCheckBox;
	@FXML private Slider difficultySlider;
	@FXML private Slider radiusSlider;
	@FXML private TextField imageFileNameField;
	
	private Tile tile;
	private Stage dialogStage;
	
	private boolean saveClicked = false;
	private final String NO_FILE_MESSAGE = "Please choose a file.";
	
	public boolean show(Tile tile, Stage owner) {
    	try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("ObstacleDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("/css/dialog.css");
	        page.getStylesheets().add("/css/style.css");
	        page.getStyleClass().add("background");
	        
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle("Obstacle");
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        dialogStage.initStyle(StageStyle.UTILITY);
	        dialogStage.setResizable(false);
	        dialogStage.initOwner(owner);
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        ObstacleDialogController controller = loader.getController();
	        controller.setDialogStage(dialogStage);
	        controller.setTile(tile);
	        
	        dialogStage.showAndWait();
	        return controller.isSaveClicked();
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public void initialize() {
		super.init();
	}
	
	public void setTile(Tile tile) {
        this.tile = tile;
        
        nameField.setDisable(true);
        forRowingCheckBox.setDisable(true);
        forCyclingCheckBox.setDisable(true);
        imageFileNameField.setDisable(true);
        
        Obstacle obstacle = tile.getObstacle();
        nameField.setText(obstacle.getName());
        
        radiusSlider.setValue(tile.getObstacleRadius() != null ?
        		tile.getObstacleRadius() : obstacle.getRadius());
        
        difficultySlider.setValue(tile.getObstacleDifficulty() != null ?
        		tile.getObstacleDifficulty() : obstacle.getDifficulty());
        
        imageFileNameField.setText(obstacle.getImageFileName() != null
        		&& !obstacle.getImageFileName().isEmpty() ?
        		obstacle.getImageFileName() : NO_FILE_MESSAGE);
    }
	
	public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
	
	public boolean isSaveClicked() {
        return saveClicked;
    }
	
	@Override
	protected String getViewTitle() {
		return "Edit Obstacle Difficulty";
	}
	
	@FXML
    private void handleSave() {
        tile.setObstacleDifficulty((int) difficultySlider.getValue());
        tile.setObstacleRadius((int) radiusSlider.getValue());
        saveClicked = true;
        dialogStage.close();
    }
	
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
}
