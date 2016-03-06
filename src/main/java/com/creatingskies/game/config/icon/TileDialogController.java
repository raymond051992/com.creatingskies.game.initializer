package com.creatingskies.game.config.icon;

import java.io.IOException;
import java.util.List;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.core.GameResult;
import com.creatingskies.game.core.Tile;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TileDialogController extends ViewController {

	@FXML private Slider difficultySlider;
	@FXML private Slider verticalTiltSlider;
	@FXML private Slider horizontalTiltSlider;
	
	private Stage dialogStage;
	private Tile tile;
	
	private boolean saveClicked = false;
	
	public boolean show(Tile tile, Stage owner) {
    	try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("TileDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("/css/dialog.css");
	        page.getStylesheets().add("/css/style.css");
	        page.getStyleClass().add("background");
	        
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle("Tile");
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        dialogStage.initStyle(StageStyle.UTILITY);
	        dialogStage.setResizable(false);
	        dialogStage.initOwner(owner);
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        TileDialogController controller = loader.getController();
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
		
		boolean allowModify = isModificationValid(tile, "edit");
		
        difficultySlider.setValue(tile.getDifficulty() != null ? tile.getDifficulty() : 0.0);
        verticalTiltSlider.setValue(tile.getVerticalTilt() != null ? tile.getVerticalTilt() : 0);
        horizontalTiltSlider.setValue(tile.getHorizontalTilt() != null ? tile.getHorizontalTilt() : 0);

        difficultySlider.setDisable(!allowModify);
        verticalTiltSlider.setDisable(!allowModify);
        horizontalTiltSlider.setDisable(!allowModify);
    }
	
	public void setDialogStage(Stage dialogStage) {
        this.dialogStage = dialogStage;
    }
	
	public boolean isSaveClicked() {
        return saveClicked;
    }
	
	@Override
	protected String getViewTitle() {
		return "Add Tile Image";
	}
	
	@FXML
    private void handleSave() {
        tile.setDifficulty((int) difficultySlider.getValue());
        tile.setVerticalTilt((int) verticalTiltSlider.getValue());
        tile.setHorizontalTilt((int) horizontalTiltSlider.getValue());
        saveClicked = true;
        dialogStage.close();
    }
	
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private boolean isModificationValid(Tile tile, String action){
		if(tile.getIdNo() != null){
			List<GameResult> results = new GameDao().findAllGameResultsByTile(tile);
			return (results == null || results.isEmpty());
		}
		return true;
	}

}
