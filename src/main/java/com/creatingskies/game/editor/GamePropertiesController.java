package com.creatingskies.game.editor;

import java.io.File;
import java.io.IOException;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.util.StringConverter;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.classes.Util;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.Game.Type;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.core.Map;
import com.creatingskies.game.core.MapDao;

public class GamePropertiesController extends PropertiesViewController{

	@FXML private TextField titleField;
	@FXML private TextArea descriptionField;
	@FXML private RadioButton gameTypeCyclingButton;
	@FXML private RadioButton gameTypeRowingButton;
	@FXML private ComboBox<Map> mapSelectionDropdown;
	@FXML private TextField audioFileNameField;
	@FXML private ButtonBar actionButtonBar;
	
	@Override
	public void init() {
		super.init();
		initFields();
	}
	
	private void initFields(){
		initMapSelections();
		titleField.textProperty().addListener((observable, oldValue, newValue) -> {
		    getGame().setTitle(newValue);
		});
		descriptionField.textProperty().addListener((observable, oldValue, newValue) -> {
		    getGame().setDescription(newValue);
		});
		gameTypeCyclingButton.setOnAction((event) -> {
		    getGame().setType(Type.CYCLING);
		});
		gameTypeRowingButton.setOnAction((event) -> {
		    getGame().setType(Type.ROWING);
		});
		mapSelectionDropdown.setOnAction((event) -> {
			getGame().setMap(mapSelectionDropdown.getSelectionModel().getSelectedItem());
		});
		
		if(getCurrentAction() == Action.VIEW){
			actionButtonBar.setVisible(false);
			disableFields(true);
		}else{
			actionButtonBar.setVisible(true);
			disableFields(false);
		}
	}
	
	private void initMapSelections(){
		MapDao mapDao = new MapDao();
		mapSelectionDropdown.setItems(FXCollections.observableArrayList(mapDao.findAllMaps()));
		mapSelectionDropdown.setConverter(new StringConverter<Map>() {
			@Override
			public String toString(Map object) {
				return object.getName();
			}
			
			@Override
			public Map fromString(String string) {
				return null;
			}
		});
	}
	
	@FXML
    private void handleAudioBrowseDialog(){
		FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Audio files", "*.ogg", "*.mp3", "*.wav", "*.wma", "*.aif");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(MainLayout.getPrimaryStage());
        
        if(file != null){
        	getGame().setAudio(Util.fileToByteArray(file));
	        getGame().setAudioFileName(file.getName());
	        getGame().setAudioFileType(Util.getFileExtension(file.getName()));
	        getGame().setAudioFileSize(file.length());
	        audioFileNameField.setText(file.getName());
        }
	}
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
        	Alert waitDialog = new AlertDialog(AlertType.INFORMATION, "Saving", null, "Please wait.");
        	waitDialog.initModality(Modality.WINDOW_MODAL);
			waitDialog.show();
			new GameDao().saveOrUpdate(getGame());
			waitDialog.hide();
			close();
			new GameController().show();
        }
    }
	
	@FXML
	private void handleCancel(){
		close();
		new GameController().show();
	}
	
	private boolean isInputValid() {
        if (Util.isBlank(titleField.getText())) {
        	new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Title is required.").showAndWait();
        	return false;
        }
        
        if(Util.isBlank(descriptionField.getText())){
        	new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Description is required.").showAndWait();
        	return false;
        }
        
        if (!gameTypeCyclingButton.isSelected() && !gameTypeRowingButton.isSelected()){
        	new AlertDialog(AlertType.ERROR, "Invalid fields", null, "Game Type is required").showAndWait();
        	return false;
        }
        
        return true;
    }
	
	@Override
	protected String getViewTitle() {
		if(getCurrentAction() == Action.ADD){
			return "Create New Game";
		}else if (getCurrentAction() == Action.EDIT){
			return "Edit Game " + getGame().getTitle();
		}else{
			return getGame().getTitle();
		}
	}
	
	private Game getGame(){
		return (Game) getCurrentRecord();
	}
	
	private void setGame(Game game){
		setCurrentRecord(game);
		titleField.setText(getGame().getTitle());
		descriptionField.setText(getGame().getDescription());
		
		if(getGame().getType() == null){
			getGame().setType(Type.CYCLING);
		}
		
		gameTypeCyclingButton.setSelected(game.getType() == Type.CYCLING);
	}
	
	public void show(Action action, Game game){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("GameProperties.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            
            GamePropertiesController controller = (GamePropertiesController) loader
					.getController();
            controller.setCurrentAction(action);
            controller.setGame(game);
            controller.init();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	private void disableFields(boolean disable){
		titleField.setDisable(disable);
		descriptionField.setDisable(disable);
		gameTypeCyclingButton.setDisable(disable);
		gameTypeRowingButton.setDisable(disable);
		mapSelectionDropdown.setDisable(disable);
		audioFileNameField.setDisable(disable);
	}
}
