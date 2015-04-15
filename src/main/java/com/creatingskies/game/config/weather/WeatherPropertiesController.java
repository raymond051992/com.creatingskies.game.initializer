package com.creatingskies.game.config.weather;

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
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.model.weather.Weather;
import com.creatingskies.game.model.weather.WeatherDAO;
import com.creatingskies.game.util.Util;

public class WeatherPropertiesController extends PropertiesViewController{

	@FXML private TextField nameField;
	@FXML private CheckBox forRowingCheckBox;
	@FXML private CheckBox forCyclingCheckBox;
	@FXML private Slider difficultySlider;
	@FXML private TextField imageFileNameField;
	@FXML private TextField audioFileNameField;
	
	private final String NO_FILE_MESSAGE = "Please choose a file.";
	
	public void show(Action action, Weather weather){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("WeatherProperties.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            
			WeatherPropertiesController controller = (WeatherPropertiesController) loader
					.getController();
            controller.setCurrentAction(action);
            controller.setWeather(weather);
            controller.init();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void setWeather(Weather weather){
		setCurrentRecord(weather);
		nameField.setText(weather.getName());
        
        difficultySlider.setValue(weather.getDifficulty() != null ?
        		weather.getDifficulty() : 0.0);
        
        forRowingCheckBox.setSelected(weather.getForRowing());
        forCyclingCheckBox.setSelected(weather.getForCycling());
        
        imageFileNameField.setText(weather.getImageFileName() != null
        		&& !weather.getImageFileName().equals("") ?
        				weather.getImageFileName() : NO_FILE_MESSAGE);
        
        audioFileNameField.setText(weather.getAudioFileName() != null
        		&& !weather.getAudioFileName().equals("") ?
        				weather.getAudioFileName() : NO_FILE_MESSAGE);
	}
	
	@Override
	public void init() {
		super.init();
	}
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
            getWeather().setName(nameField.getText());
            getWeather().setDifficulty((int) difficultySlider.getValue());
            getWeather().setForRowing(forRowingCheckBox.isSelected());
            getWeather().setForCycling(forCyclingCheckBox.isSelected());
            
            new WeatherDAO().saveOrUpdate(getWeather());
            new WeathersController().show();
        }
    }
	
    @FXML
    private void handleCancel() {
        new WeathersController().show();
    }
    
    private boolean isInputValid() {
    	String errorMessage = "";

        if (nameField.getText() == null || nameField.getText().length() == 0) {
            errorMessage += "Weather name is required.\n"; 
        }
        
        if (!forRowingCheckBox.isSelected() && !forCyclingCheckBox.isSelected()){
        	errorMessage += "At least one game type should be selected.\n";
        }
        
        if(imageFileNameField.getText().equals(NO_FILE_MESSAGE)){
        	errorMessage += "Image for weather is required.\n";
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
        
        getWeather().setImage(Util.fileToByteArray(file));
        getWeather().setImageFileName(file != null ? file.getName() : null);
        getWeather().setImageFileType(Util.getFileExtension(getWeather()
				.getImageFileName()));
        
        imageFileNameField.setText(getWeather().getImageFileName() != null
        		&& !getWeather().getImageFileName().equals("") ?
        		getWeather().getImageFileName() : NO_FILE_MESSAGE);
	}
    
    @FXML
    private void handleAudioBrowseDialog(){
		FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Audio files", "*.ogg", "*.mp3", "*.wav", "*.wma", "*.aif");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(MainLayout.getPrimaryStage());
        
        getWeather().setAudio(Util.fileToByteArray(file));
        getWeather().setAudioFileName(file != null ? file.getName() : null);
        getWeather().setAudioFileType(Util.getFileExtension(getWeather()
				.getAudioFileName()));
        
        audioFileNameField.setText(getWeather().getAudioFileName() != null
        		&& !getWeather().getAudioFileName().equals("") ?
        		getWeather().getAudioFileName() : NO_FILE_MESSAGE);
	}
	
	private Weather getWeather(){
		return (Weather) getCurrentRecord();
	}
	
	@Override
	protected String getViewTitle() {
		if(getCurrentAction() == Action.ADD){
			return "Create New Weather";
		} else if (getCurrentAction() == Action.EDIT) {
			return "Edit Weather " + getWeather().getName();
		} else {
			return "Weather " + getWeather().getName();
		}
	}

}
