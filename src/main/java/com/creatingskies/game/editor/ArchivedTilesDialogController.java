package com.creatingskies.game.editor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import com.creatingskies.game.core.MapDao;
import com.creatingskies.game.core.TileImage;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.util.Util;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.effect.DropShadow;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.paint.Color;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

public class ArchivedTilesDialogController {
	
	@FXML private ImageView selectedTileImageView;
	@FXML private Label selectedTileDescription;
	@FXML private FlowPane tileImageSelections;
	@FXML private Button restoreSelectedTilesButton;
	
	private List<TileImage> selectedTileImages = new ArrayList<TileImage>();
	
	private Stage dialogStage;
	
	public void show(Stage owner) {
	    try {
	        FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("ArchivedTilesDialog.fxml"));
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

	        ArchivedTilesDialogController controller = loader.getController();
	        controller.setDialogStage(dialogStage);
	        controller.init();
	        
	        dialogStage.showAndWait();;
	    } catch (IOException e) {
	        e.printStackTrace();
	    }
	}
	
	public void init(){
		selectedTileImages.clear();
		tileImageSelections.getChildren().clear();
		
		MapDao mapDao = new MapDao();
		List<TileImage> tileImages = mapDao.findAllArchivedTileImages();
		if(tileImages != null && !tileImages.isEmpty()){
			for(TileImage tileImage : tileImages){
				addTileImageSelection(tileImage);
			}
		}
	}
	
	private void addTileImageSelection(TileImage tileImage){
		ImageView imageView = new ImageView(Util.byteArrayToImage(tileImage.getImage()));
		imageView.setFitHeight(Constant.TILE_HEIGHT);
		imageView.setFitWidth(Constant.TILE_WIDTH);
		
		imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				handleTileImageSelection(tileImage, imageView.getImage());
				
				if(event.isControlDown()){
					DropShadow ds = new DropShadow( 20, Color.RED );
					imageView.setEffect(ds);
					selectedTileImages.add(tileImage);
				}else{
					removeAllSelectedTileImage();
				}
			}
		});
		
		tileImageSelections.getChildren().add(imageView);
	}
	
	private void removeAllSelectedTileImage(){
		if(tileImageSelections.getChildren() != null){
			for(Node node : tileImageSelections.getChildren()){
				if(node instanceof ImageView){
					node.setEffect(null);
				}
			}
			selectedTileImages = new ArrayList<TileImage>();
		}
	}
	
	private void handleTileImageSelection(TileImage tileImage, Image image){
		selectedTileImageView.setImage(image);
		
		selectedTileDescription
			.setText("Type: Tile \n"
				+ "File Name: " + tileImage.getFileName() + "\n"
				+ "Difficulty: " + tileImage.getDifficulty() + "\n"
				+ "Vertical Tilt: " + (tileImage.getVerticalTilt() != null ? tileImage.getVerticalTilt() : 0) + "\n"
				+ "Horizontal Tilt: " + (tileImage.getHorizontalTilt() != null ? tileImage.getHorizontalTilt() : 0) + "\n"
				+ "Radius: 0");
	}
	
	@FXML
	private void restore(){
		if(selectedTileImages != null){
			MapDao mapDao = new MapDao();
			for(TileImage tileImage : selectedTileImages){
				tileImage.setArchived(false);
				mapDao.saveOrUpdate(tileImage);
			}
			init();
		}
	}
	
	
	@FXML
	private void close(){
		dialogStage.close();
	}
	
	
	private void setDialogStage(Stage stage){
		this.dialogStage = stage;
	}
}
