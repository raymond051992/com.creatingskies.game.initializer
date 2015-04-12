package com.creatingskies.game.config.obstacle;

import java.io.File;
import java.io.IOException;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.CheckBox;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.classes.Util;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.model.obstacle.Obstacle;
import com.creatingskies.game.model.obstacle.ObstacleDAO;

public class ObstaclePropertiesController extends PropertiesViewController {

	@FXML private TextField nameField;
	@FXML private CheckBox forRowingCheckBox;
	@FXML private CheckBox forCyclingCheckBox;
	@FXML private Slider difficultySlider;
	@FXML private TextField imageFileNameField;
	
	private final String NO_FILE_MESSAGE = "Please choose a file.";
	
	public void show(Action action, Obstacle obstacle){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("ObstacleProperties.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            
			ObstaclePropertiesController controller = (ObstaclePropertiesController) loader
					.getController();
            controller.setCurrentAction(action);
            controller.setObstacle(obstacle);
            controller.init();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void setObstacle(Obstacle obstacle){
		setCurrentRecord(obstacle);
		nameField.setText(obstacle.getName());
        
        difficultySlider.setValue(obstacle.getDifficulty() != null ?
        		obstacle.getDifficulty() : 0.0);
        
        forRowingCheckBox.setSelected(obstacle.getForRowing());
        forCyclingCheckBox.setSelected(obstacle.getForCycling());
        
        imageFileNameField.setText(getObstacle().getImageFileName() != null
        		&& !getObstacle().getImageFileName().equals("") ?
        		getObstacle().getImageFileName() : NO_FILE_MESSAGE);
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
            getObstacle().setName(nameField.getText());
            getObstacle().setDifficulty((int) difficultySlider.getValue());
            getObstacle().setForRowing(forRowingCheckBox.isSelected());
            getObstacle().setForCycling(forCyclingCheckBox.isSelected());
            
            new ObstacleDAO().saveOrUpdate(getObstacle());
            new ObstaclesController().show();
        }
    }
	
    @FXML
    private void handleCancel() {
        new ObstaclesController().show();
    }
    
    private boolean isInputValid() {
    	String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "Obstacle name is required.\n"; 
        }
        
        if (!forRowingCheckBox.isSelected() && !forCyclingCheckBox.isSelected()){
        	errorMessage += "At least one game type should be selected.\n";
        }
        
        if(imageFileNameField.getText().equals(NO_FILE_MESSAGE)){
        	errorMessage += "Image for obstacle is required.\n";
        }
        
        if (errorMessage.length() == 0) {
            return true;
        } else {
			new AlertDialog(AlertType.ERROR, "Invalid fields",
					"Please correct invalid fields.", errorMessage)
					.showAndWait();
            return false;
        }
    }
    
    @FXML
    private void handleImageBrowseDialog(){
		FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Image files", "*.jpeg", "*.jpg", "*.png", "*.bmp", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(MainLayout.getPrimaryStage());
        
        getObstacle().setImage(Util.fileToByteArray(file));
        getObstacle().setImageFileName(file != null ? file.getName() : null);
        getObstacle().setImageFileType(Util.getFileExtension(getObstacle()
				.getImageFileName()));
        
        imageFileNameField.setText(getObstacle().getImageFileName() != null
        		&& !getObstacle().getImageFileName().equals("") ?
        		getObstacle().getImageFileName() : NO_FILE_MESSAGE);
	}
    
	private Obstacle getObstacle(){
		return (Obstacle) getCurrentRecord();
	}
	
	@Override
	protected String getViewTitle() {
		if(getCurrentAction() == Action.ADD){
			return "Create New Obstacle";
		} else if (getCurrentAction() == Action.EDIT) {
			return "Edit Obstacle " + getObstacle().getName();
		} else {
			return "Obstacle " + getObstacle().getName();
		}
	}

}
