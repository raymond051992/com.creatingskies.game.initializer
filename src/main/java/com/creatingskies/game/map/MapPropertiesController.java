package com.creatingskies.game.map;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.stage.Modality;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Map;
import com.creatingskies.game.core.MapDao;
import com.creatingskies.game.core.Tile;

public class MapPropertiesController extends PropertiesViewController{
	
	@FXML private GridPane fieldContainer;
	@FXML private TextField nameTextField;
	@FXML private TextArea descriptionTextField;
	@FXML private TextField widthTextField;
	@FXML private TextField heightTextField;
	
	@FXML private Button openDesignerButton;
	@FXML private Button saveButton;
	@FXML private Button cancelButton;
	
	@Override
	public void init() {
		super.init();
		initWidthAndHeightTextFieldKeyValidation();
		initFields();
		
		fieldContainer.setDisable(getCurrentAction() == Action.VIEW);
		saveButton.setVisible(getCurrentAction() != Action.VIEW);
		
		if(getCurrentAction() == Action.VIEW){
			cancelButton.setText("OK");
		}else{
			cancelButton.setText("Cancel");
		}
	}
	
	private void initFields(){
		if(getMap() != null){
			nameTextField.setText(getMap().getName());
			descriptionTextField.setText(getMap().getDescription());
			if(getMap().getWidth() != null){
				widthTextField.setText(String.valueOf(getMap().getWidth()));
			}
			if(getMap().getHeight() != null){
				heightTextField.setText(String.valueOf(getMap().getHeight()));
			}
			
		}
	}
	
	private void initWidthAndHeightTextFieldKeyValidation(){
		widthTextField.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (!t.getCharacter().matches("\\d")) {
                    t.consume();
                }
            }
        });
		
		heightTextField.addEventFilter(KeyEvent.KEY_TYPED, new EventHandler<KeyEvent>() {
            @Override
            public void handle(KeyEvent t) {
                if (!t.getCharacter().matches("\\d")) {
                    t.consume();
                }
            }
        });
	}
	
	@FXML
	private void showMapDesigner(){
		if(isValidDetails()){
			mapDetails();
			new MapDesignerController().show(getCurrentAction(),getMap());
			close();
		}
	}
	
	private boolean isValidDetails(){
		String errorMessage = "";
		
		if(nameTextField.getText() == null || nameTextField.getText().isEmpty()){
			errorMessage += "Name is required.\n";
		}
		
		if(descriptionTextField.getText() == null || descriptionTextField.getText().isEmpty()){
			errorMessage += "Description is required.\n";
		}
		
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
		if(getMap().getTiles() == null || getMap().getTiles().isEmpty()){
			new AlertDialog(AlertType.ERROR, "Oops", "", "Please design your map.").showAndWait();
			return false;
		}
		if(getMap().getStartPoint() == null){
			new AlertDialog(AlertType.ERROR, "Oops", "", "Please assign start point to the map.").showAndWait();
			return false;
		}
		if(getMap().getEndPoint() == null){
			new AlertDialog(AlertType.ERROR, "Oops", "", "Please assign an end point to the map.").showAndWait();
			return false;
		}
		
		return true;
	}
	
	private void mapDetails(){
		if(!getMap().isReady()){
			getMap().setName(nameTextField.getText());
			getMap().setDescription(descriptionTextField.getText());
			getMap().setWidth(Integer.parseInt(widthTextField.getText()));
			getMap().setHeight(Integer.parseInt(heightTextField.getText()));
			
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
	
	@FXML
	private void save(){
		if(isValidDetails() && isValidMap()){
			Alert waitDialog = new AlertDialog(AlertType.INFORMATION, "Saving", null, "Please wait.");
			waitDialog.initModality(Modality.WINDOW_MODAL);
			waitDialog.show();
			new MapDao().saveOrUpdate(getMap());
			waitDialog.hide();
			close();
			new MapController().show();
		}
	}
	
	@FXML 
	private void cancel(){
		close();
		new MapController().show();
	}
	
	private Map getMap(){
		return (Map) getCurrentRecord();
	}
	
	@Override
	protected String getViewTitle() {
		if(getCurrentAction() == Action.ADD){
			return "Create New Map";
		}else if (getCurrentAction() == Action.EDIT){
			return "Edit Map "+ getMap().getName();
		}else {
			return "Map " + getMap().getName();
		}
	}
	
	public void show(Action action,Map map){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("MapProperties.fxml"));
            AnchorPane pane = (AnchorPane) loader.load();
            
            MapPropertiesController controller = (MapPropertiesController)loader.getController();
            controller.setCurrentAction(action);
            controller.setCurrentRecord(map);
            controller.init();
            MainLayout.getRootLayout().setCenter(pane);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	
}

