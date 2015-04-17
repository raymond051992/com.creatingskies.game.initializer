package com.creatingskies.game.editor;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ButtonBar;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.Game.Type;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.core.Map;
import com.creatingskies.game.core.MapDao;
import com.creatingskies.game.core.Tile;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.util.Util;

public class GamePropertiesController extends PropertiesViewController{

	@FXML private TextField titleField;
	@FXML private TextArea descriptionField;
	@FXML private RadioButton gameTypeCyclingButton;
	@FXML private RadioButton gameTypeRowingButton;
	@FXML private TextField audioFileNameField;
	@FXML private Button browseButton;
	@FXML private ButtonBar actionButtonBar;
	
	@FXML private TextField widthTextField;
	@FXML private TextField heightTextField;
	@FXML private Button openDesignerButton;
	
	@Override
	public void init() {
		super.init();
		initFields();
	}
	
	private void initFields(){
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
		
		if(getCurrentAction() == Action.VIEW){
			actionButtonBar.setVisible(false);
			disableFields(true);
		}else{
			actionButtonBar.setVisible(true);
			disableFields(false);
		}
		
		widthTextField.addEventFilter(KeyEvent.KEY_TYPED, Util.createIntegerOnlyKeyEvent());
		heightTextField.addEventFilter(KeyEvent.KEY_TYPED, Util.createIntegerOnlyKeyEvent());
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
        if (isInputValid() && isValidDetails() && isValidMap()) {
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
		String errorMessage = "";
		
        if (Util.isBlank(titleField.getText())) {
        	errorMessage += "Title is required.\n";
        }
        
        if(Util.isBlank(descriptionField.getText())){
        	errorMessage += "Description is required.\n";
        }
        
        if (!gameTypeCyclingButton.isSelected() && !gameTypeRowingButton.isSelected()){
        	errorMessage += "Game Type is required.\n";
        }
        
        if(!errorMessage.isEmpty()){
			new AlertDialog(AlertType.ERROR, "Oops", "", errorMessage).showAndWait();
		}

		return errorMessage.isEmpty();
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
		widthTextField.setText(getMap().getWidth() != null ?
				getMap().getWidth().toString() : "");
		heightTextField.setText(getMap().getHeight() != null ?
				getMap().getHeight().toString() : "");
		
		if(getGame().getType() == null){
			getGame().setType(Type.CYCLING);
		}
		
		gameTypeCyclingButton.setSelected(game.getType() == Type.CYCLING);
		gameTypeRowingButton.setSelected(game.getType() == Type.ROWING);
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
		audioFileNameField.setDisable(disable);
		widthTextField.setDisable(disable);
		heightTextField.setDisable(disable);
		browseButton.setDisable(disable);
	}
	
	@FXML
	private void showMapDesigner(){
		if(isValidDetails()){
			Alert waitDialog = new AlertDialog(AlertType.INFORMATION, "Loading resources", null, "Please wait.");
        	waitDialog.initModality(Modality.WINDOW_MODAL);
			waitDialog.show();
			
			if(getCurrentAction() != Action.ADD){
				Map map = new MapDao().findMapWithDetails(getGame().getMap().getIdNo());
				getGame().setMap(map);
			}
			
			loadMapDetails();
			new MapDesignerController().show(getCurrentAction(), getGame());
			close();
			waitDialog.hide();
		}
	}
	
	private void loadMapDetails(){
		if(!getMap().isReady()){
			getMap().setWidth(Integer.parseInt(widthTextField.getText()));
			getMap().setHeight(Integer.parseInt(heightTextField.getText()));
			
			MapDao mapDao = new MapDao();
			String owner = gameTypeRowingButton.isSelected() ?
					Constant.IMAGE_ROWING_TILE_OWNER : Constant.IMAGE_CYCLING_TILE_OWNER;
			getMap().setDefaultTileImage(mapDao.findTileImageByOwner(owner));
			
			List<Tile> tiles = new ArrayList<Tile>();
			for (int r = 0; r < getMap().getHeight(); r++) {
				for (int c = 0; c < getMap().getWidth(); c++) {
					Tile tile = new Tile();
					tile.setMap(getMap());
					tile.setColIndex(c);
					tile.setRowIndex(r);
					tiles.add(tile);
				}
			}
			getMap().setTiles(tiles);
		}
	}
	
	private boolean isValidDetails(){
		String errorMessage = "";
		
		if(widthTextField.getText() == null || widthTextField.getText().isEmpty()){
			errorMessage += "Width is required.\n";
		} else if(Integer.parseInt(widthTextField.getText()) <= 0){
			errorMessage += "Width should be 1 or greater.\n";
		}
		
		if(heightTextField.getText() == null || heightTextField.getText().isEmpty()){
			errorMessage += "Height is required.\n";
		} else if(Integer.parseInt(heightTextField.getText()) <= 0){
			errorMessage += "Height should be 1 or greater.\n";
		}
		
		if(!errorMessage.isEmpty()){
			new AlertDialog(AlertType.ERROR, "Oops", "", errorMessage).showAndWait();
		}

		return errorMessage.isEmpty();
	}
	
	private Boolean isValidMap(){
		if(getMap().getTiles() == null || getMap().getStartPoint() == null || getMap().getEndPoint() == null){
			new AlertDialog(AlertType.ERROR, "Oops", "", "Please design your map.").showAndWait();
			return false;
		}

		return true;
	}
	
	private Map getMap(){
		return getGame().getMap();
	}
	
}
