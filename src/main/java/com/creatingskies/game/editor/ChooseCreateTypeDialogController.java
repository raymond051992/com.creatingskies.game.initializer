package com.creatingskies.game.editor;

import java.io.IOException;

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

import com.creatingskies.game.classes.ViewController.Action;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.GameDao;

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
			new GamePropertiesController().show(Action.ADD, new Game());
		}else{
			if(gameComboBox.getSelectionModel().getSelectedItem() != null){
				new GamePropertiesController().createNewFromExistingGame(gameComboBox.getSelectionModel().getSelectedItem());
			}
		}
	}
	
	@FXML
	private void cancel(){
		stage.close();
	}
}
