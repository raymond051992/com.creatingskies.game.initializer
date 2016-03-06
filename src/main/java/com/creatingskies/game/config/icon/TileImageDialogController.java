package com.creatingskies.game.config.icon;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.core.GameDao;
import com.creatingskies.game.core.GameResult;
import com.creatingskies.game.core.MapDao;
import com.creatingskies.game.core.TileImage;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.util.Util;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class TileImageDialogController extends ViewController {

	@FXML private TextField nameField;
	@FXML private TextField fileNameField;
	@FXML private ImageView previewImage;
	
	@FXML private Label notApplicableDifficultyLabel;
	@FXML private Label notApplicableVerticalTiltLabel;
	@FXML private Label notApplicableHorizontalTiltLabel;
	
	@FXML private Slider difficultySlider;
	@FXML private Slider verticalTiltSlider;
	@FXML private Slider horizontalTiltSlider;
	
	private Stage dialogStage;
	private TileImage tileImage;
	private Image defaultImage;
	
	private boolean saveClicked = false;
	private final String NO_FILE_MESSAGE = "Please choose a file.";
	
	public boolean show(TileImage tileImage, Stage owner) {
    	try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("TileImageDialog.fxml"));
	        AnchorPane page = (AnchorPane) loader.load();

	        page.getStylesheets().add("/css/dialog.css");
	        page.getStylesheets().add("/css/style.css");
	        page.getStyleClass().add("background");
	        
	        Stage dialogStage = new Stage();
	        dialogStage.setTitle("TileImage");
	        dialogStage.initModality(Modality.WINDOW_MODAL);
	        dialogStage.initStyle(StageStyle.UTILITY);
	        dialogStage.setResizable(false);
	        dialogStage.initOwner(owner);
	        Scene scene = new Scene(page);
	        dialogStage.setScene(scene);

	        TileImageDialogController controller = loader.getController();
	        controller.setDialogStage(dialogStage);
	        controller.setTileImage(tileImage);
	        
	        dialogStage.showAndWait();
	        return controller.isSaveClicked();
	    } catch (IOException e) {
	        e.printStackTrace();
	        return false;
	    }
	}
	
	public void initialize() {
		super.init();
		fileNameField.setText(NO_FILE_MESSAGE);
		defaultImage = Util.byteArrayToImage(Util
				.stringUrlToByteArray(Constant.PATH_TILE_IMAGE_PLACEHOLDER));
	}
	
	public void setTileImage(TileImage tileImage) {
        this.tileImage = tileImage;
        
        boolean allowModify = isModificationValid(tileImage, "edit");
        
        nameField.setDisable(tileImage.getSystemDefined() || !allowModify);
        nameField.setText(tileImage.getOwner());
        
        notApplicableDifficultyLabel.setVisible(tileImage.getDifficulty() == null);
        notApplicableVerticalTiltLabel.setVisible(tileImage.getVerticalTilt() == null);
        notApplicableHorizontalTiltLabel.setVisible(tileImage.getHorizontalTilt() == null);

        difficultySlider.setVisible(tileImage.getDifficulty() != null);
        verticalTiltSlider.setVisible(tileImage.getVerticalTilt() != null);
        horizontalTiltSlider.setVisible(tileImage.getHorizontalTilt() != null);
        
        difficultySlider.setValue(tileImage.getDifficulty() != null ? tileImage.getDifficulty() : 0.0);
        verticalTiltSlider.setValue(tileImage.getVerticalTilt() != null ? tileImage.getVerticalTilt() : 0);
        horizontalTiltSlider.setValue(tileImage.getHorizontalTilt() != null ? tileImage.getHorizontalTilt() : 0);

        difficultySlider.setDisable(!allowModify);
        verticalTiltSlider.setDisable(!allowModify);
        horizontalTiltSlider.setDisable(!allowModify);
        
        fileNameField.setText(tileImage.getFileName() != null
        		&& !tileImage.getFileName().equals("") ?
        		tileImage.getFileName() : NO_FILE_MESSAGE);
        
		previewImage.setImage(tileImage.getImage() != null ? Util.byteArrayToImage(tileImage.getImage()) : defaultImage);
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
	private void handleOpenBrowseDialog(){
		FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Image files", "*.jpeg", "*.jpg", "*.png", "*.bmp", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(dialogStage);
        
        if(file != null){
        	tileImage.setImage(Util.fileToByteArray(file));
            tileImage.setFileName(file != null ? file.getName() : null);
    		tileImage.setFileType(Util.getFileExtension(tileImage
    				.getFileName()));
    		tileImage.setFileSize(-1L);
        }
        
		fileNameField.setText(tileImage.getFileName() != null
        		&& !tileImage.getFileName().equals("") ?
        		tileImage.getFileName() : NO_FILE_MESSAGE);
		
		previewImage.setImage(tileImage.getImage() != null ? Util.byteArrayToImage(tileImage.getImage()) : defaultImage);
	}
	
	@FXML
    private void handleSave() {
        if (isInputValid()) {
            tileImage.setDifficulty((int) difficultySlider.getValue());
            tileImage.setVerticalTilt((int) verticalTiltSlider.getValue());
            tileImage.setHorizontalTilt((int) horizontalTiltSlider.getValue());
            tileImage.setOwner(nameField.getText());
            saveClicked = true;
            dialogStage.close();
        }
    }
	
    @FXML
    private void handleCancel() {
        dialogStage.close();
    }
    
    private boolean isInputValid() {
        String errorMessage = "";

        if(nameField.getText() == null || nameField.getText().isEmpty()){
        	errorMessage += "Name is required.\n";
        }
        
        if(fileNameField.getText().equals(NO_FILE_MESSAGE)){
        	errorMessage += "Image is required.\n";
        }
        
        if(tileImage.getIdNo() == null || !tileImage.getOwner().equals(nameField.getText())){
        	TileImage existingTileImage = new MapDao().findTileImageByOwner(nameField.getText());
            if(existingTileImage != null){
            	errorMessage += "Name already exists.\n";
            }
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
    
    private boolean isModificationValid(TileImage tileImage, String action){
		if(tileImage.getIdNo() != null){
			List<GameResult> results = new GameDao().findAllGameResultsByTileImage(tileImage);
			return (results == null || results.isEmpty());
		}
		return true;
	}

}
