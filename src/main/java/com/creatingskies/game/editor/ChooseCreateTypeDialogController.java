package com.creatingskies.game.editor;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.ComboBox;
import javafx.scene.control.RadioButton;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.HBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.StringConverter;

import org.apache.commons.beanutils.PropertyUtils;

import com.creatingskies.game.classes.ViewController.Action;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.core.Map;
import com.creatingskies.game.core.Tile;
import com.creatingskies.game.util.Logger;
import com.creatingskies.game.util.Logger.Level;

public class ChooseCreateTypeDialogController {

	@FXML private RadioButton createNewRadioButton;
	@FXML private RadioButton copyFromExistingRadioButton;
	@FXML private HBox gameSelectionHBox;
	@FXML private ComboBox<Game> gameComboBox;
	
	
	private Stage stage;
	
	public void show() {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("ChooseCreateTypeDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("/css/dialog.css");
	        page.getStylesheets().add("/css/style.css");
	        page.getStyleClass().add("background");
			
	        stage = new Stage();
	        stage.setTitle("Company");
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        stage.initStyle(StageStyle.UTILITY);
	        stage.setResizable(false);
	        Scene scene = new Scene(page);
	        stage.setScene(scene);

	        ChooseCreateTypeDialogController controller = loader.getController();
	        controller.setStage(stage);
	        controller.initFields();
	        
	        stage.show();
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	private void setStage(Stage stage){
		this.stage = stage;
	}
	
	private void initFields(){
		gameSelectionHBox.setVisible(false);
		
		createNewRadioButton.setOnAction((event) -> {
			if(createNewRadioButton.isSelected()){
				gameSelectionHBox.setVisible(false);
			}else{
				gameSelectionHBox.setVisible(true);
			}
		});
		
		copyFromExistingRadioButton.setOnAction((event) -> {
			if(copyFromExistingRadioButton.isSelected()){
				gameSelectionHBox.setVisible(true);
			}else{
				gameSelectionHBox.setVisible(false);
			}
		});
		
		gameComboBox.setItems(FXCollections.observableArrayList(new GameDao().findAllGames()));
		gameComboBox.setConverter(new StringConverter<Game>() {
			
			@Override
			public String toString(Game object) {
				return object.getTitle();
			}
			
			@Override
			public Game fromString(String string) {
				return null;
			}
		});
	}
	
	@FXML
	private void next(){
		stage.close();
		if(createNewRadioButton.isSelected()){
			new GamePropertiesController().show(Action.ADD, new Game(),false,false);
		}else{
			if(gameComboBox.getSelectionModel().getSelectedItem() != null){
				try {
					Game newGame = new Game();
					Game gameToCopy = new GameDao().findGameWithDetails(gameComboBox.getSelectionModel().getSelectedItem().getIdNo());
					PropertyUtils.copyProperties(newGame, gameToCopy);
					Map mapToCopy = gameToCopy.getMap();
					
					newGame.setIdNo(null);
					newGame.setEditBy(null);
					newGame.setEditDate(null);
					newGame.setEntryBy(null);
					newGame.setEntryDate(null);
					newGame.getMap().setIdNo(null);
					
					Map map = new Map();
					map.setDefaultTileImage(mapToCopy.getDefaultTileImage());
					map.setHeight(mapToCopy.getHeight());
					map.setWidth(mapToCopy.getWidth());
					map.setTiles(new ArrayList<Tile>());
					map.setWeathers(null);
					
					
					List<Tile> tiles = new ArrayList<Tile>(newGame.getMap().getTiles());
					for(Tile tile : tiles){
						tile.setIdNo(null);
						tile.setMap(map);
					}
					map.setTiles(tiles);
					newGame.setMap(map);
					
					new GamePropertiesController().show(Action.ADD, newGame,false,true);
				} catch (IllegalAccessException | InvocationTargetException
						| NoSuchMethodException e) {
					Logger.log(getClass(), Level.ERROR, e.getMessage(),e);
				}
			}
		}
	}
	
	@FXML
	private void cancel(){
		stage.close();
	}
}
