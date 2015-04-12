package com.creatingskies.game.map;

import java.io.File;
import java.io.IOException;
import java.util.List;

import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.SplitPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.creatingskies.game.classes.Util;
import com.creatingskies.game.classes.ViewController.Action;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Map;
import com.creatingskies.game.core.MapDao;
import com.creatingskies.game.core.Tile;
import com.creatingskies.game.core.TileImage;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.obstacle.Obstacle;
import com.creatingskies.game.model.obstacle.ObstacleDAO;

public class MapDesignerController {
	
	@FXML private SplitPane mapDesignerContainer;
	@FXML private AnchorPane mapContainer;
	
	@FXML private GridPane mapTiles;
	@FXML private Label viewTitle;
	
	@FXML private FlowPane tileImageSelections;
	@FXML private FlowPane obstacleImageSelections;
	@FXML private FlowPane requiredTileSelections;
	@FXML private VBox requiredBox;
	
	@FXML private ImageView selectedTileImageView;
	private byte[] selectedTileImageByteArray;
	
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private Obstacle selectedObstacle;
	private Pane startTilePane;
	private Pane endTilePane;
	private boolean startTileSelected;
	private boolean endTileSelected;
	
	private Map map;
	private Stage stage;
	
	private Action currentAction;
	
	public void init(){
		initMapTiles();
		initTileImageSelections();
		initObstacleSelections();
		initRequiredTiles();
		validateRequiredTiles();
		
		mapDesignerContainer.setDisable(getCurrentAction() == Action.VIEW);
		saveButton.setVisible(getCurrentAction() != Action.VIEW);
		cancelButton.setText(getCurrentAction() == Action.VIEW ? "OK" : "Cancel");
	}
	
	private void initMapTiles(){
		mapTiles.getChildren().clear();
		for(Tile tile : map.getTiles()){
			ImageView imageView = new ImageView();
			imageView.setFitHeight(Constant.TILE_HEIGHT);
			imageView.setFitWidth(Constant.TILE_WIDTH);
			imageView.setImage(Util.byteArrayToImage(tile.getImage()));
			Pane tilePane = new Pane(imageView);
			tilePane.getStyleClass().add("map-designer-tile");
			mapTiles.add(tilePane, tile.getColIndex(), tile.getRowIndex());
			
			tilePane.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					handlePaintTile(tile, imageView);
				}
			});
			
			tilePane.setOnMouseEntered(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if(event.isAltDown()){
						handlePaintTile(tile, imageView);	
					}
				}
			});
		}
	}
	
	private void handlePaintTile(Tile tile, ImageView imageView) {
		if(getCurrentAction() != null && selectedTileImageView.getImage() != null){
			tile.setImage(selectedTileImageByteArray);
			imageView.setImage(selectedTileImageView.getImage());
			
			if(tile.getStartPoint()){
				tile.setStartPoint(false);
				requiredTileSelections.getChildren().add(startTilePane);
			}
			
			if(tile.getEndPoint()){
				tile.setEndPoint(false);
				requiredTileSelections.getChildren().add(endTilePane);
			}
			
			if(startTileSelected || endTileSelected){
				selectedTileImageView.setImage(null);
				tile.setStartPoint(startTileSelected);
				tile.setEndPoint(endTileSelected);
			}
			
			validateRequiredTiles();
			
			tile.setObstacleDifficulty(selectedObstacle != null ? selectedObstacle
					.getDifficulty() : null);
		}
	}
	
	private void validateRequiredTiles(){
		if(map.getStartPoint() != null){
			requiredTileSelections.getChildren().remove(startTilePane);
		}
		
		if(map.getEndPoint() != null){
			requiredTileSelections.getChildren().remove(endTilePane);
		}
		
		requiredBox.setVisible(!requiredTileSelections.getChildren().isEmpty());
	}
	
	private void initTileImageSelections(){
		tileImageSelections.getChildren().clear();
		MapDao mapDao = new MapDao();
		List<TileImage> tileImages = mapDao.findAllTileImages();
		addTileImageSelection(new TileImage());
		if(tileImages != null && !tileImages.isEmpty()){
			for(TileImage tileImage : tileImages){
				addTileImageSelection(tileImage);
			}
		}
	}
	
	private void initObstacleSelections(){
		obstacleImageSelections.getChildren().clear();
		ObstacleDAO obstacleDAO = new ObstacleDAO();
		List<Obstacle> obstacles = obstacleDAO.findAll();
		
		if(obstacles != null && !obstacles.isEmpty()){
			for(Obstacle obstacle : obstacles){
				addObstacleSelection(obstacle);
			}
		}
	}
	
	private void initRequiredTiles(){
		requiredTileSelections.getChildren().clear();
		initStartTile();
		initEndTile();
	}
	
	private void initStartTile(){
		Image image = new Image(Constant.PATH_TILE_IMAGE_START_POINT);
		ImageView imageView = new ImageView(image);
		imageView.setFitHeight(Constant.TILE_HEIGHT);
		imageView.setFitWidth(Constant.TILE_WIDTH);
		startTilePane = new Pane(imageView);
		
		startTilePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(imageView.getImage());
				selectedTileImageByteArray = Util.imageToByteArray(image, "png");
				selectedObstacle = null;
				
				startTileSelected = true;
				endTileSelected = false;
			}
		});
		
		requiredTileSelections.getChildren().add(startTilePane);
	}
	
	private void initEndTile(){
		Image image = new Image(Constant.PATH_TILE_IMAGE_END_POINT);
		ImageView imageView = new ImageView(image);
		imageView.setFitHeight(Constant.TILE_HEIGHT);
		imageView.setFitWidth(Constant.TILE_WIDTH);
		endTilePane = new Pane(imageView);
		
		endTilePane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(imageView.getImage());
				selectedTileImageByteArray = Util.imageToByteArray(image, "png");
				selectedObstacle = null;
				
				endTileSelected = true;
				startTileSelected = false;
			}
		});
		
		requiredTileSelections.getChildren().add(endTilePane);
	}
	
	private void addObstacleSelection(Obstacle obstacle){
		ImageView imageView = new ImageView(Util.byteArrayToImage(obstacle.getImage()));
		imageView.setFitHeight(Constant.TILE_HEIGHT);
		imageView.setFitWidth(Constant.TILE_WIDTH);
		Pane pane = new Pane(imageView);
		
		pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(imageView.getImage());
				selectedTileImageByteArray = obstacle.getImage();
				selectedObstacle = obstacle;
				
				startTileSelected = false;
				endTileSelected = false;
			}
		});
		
		obstacleImageSelections.getChildren().add(pane);
	}
	
	private void addTileImageSelection(TileImage tileImage){
		ImageView imageView = new ImageView(Util.byteArrayToImage(tileImage.getImage()));
		imageView.setFitHeight(Constant.TILE_HEIGHT);
		imageView.setFitWidth(Constant.TILE_WIDTH);
		Pane pane = new Pane(imageView);
		
		if(imageView.getImage() == null){
			pane.getStyleClass().add("map-blank-tile");
		}
		
		pane.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(imageView.getImage());
				selectedTileImageByteArray = tileImage.getImage();
				selectedObstacle = null;
				
				startTileSelected = false;
				endTileSelected = false;
			}
		});
		
		tileImageSelections.getChildren().add(pane);
	}
	
	public void show(Action action, Map map){
		try{
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("Designer.fxml"));
	        AnchorPane designer = (AnchorPane) loader.load();
	        
	        designer.getStylesheets().add("/css/style.css");
	        stage = new Stage();
	        stage.setTitle(map.getName());
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        stage.initStyle(StageStyle.UNDECORATED);
	        Scene scene = new Scene(designer);
	        stage.setMaximized(true);
	        stage.setScene(scene);
	        
	        MapDesignerController controller = (MapDesignerController) loader.getController();
	        controller.setMap(map);
	        controller.setStage(stage);
        	controller.setViewTitle(map.getName());
	        
	        controller.setCurrentAction(action);
	        controller.init();
	        stage.showAndWait();
		}catch(IOException e){
			e.printStackTrace();
		}
	}
	
	@FXML
	private void uploadTileImage(){
		FileChooser fileChooser = new FileChooser();

        FileChooser.ExtensionFilter extFilter = new FileChooser.ExtensionFilter(
                "Image files", "*.jpeg", "*.jpg", "*.png", "*.bmp", "*.gif");
        fileChooser.getExtensionFilters().add(extFilter);

        File file = fileChooser.showOpenDialog(MainLayout.getPrimaryStage());
        
        if(file != null){
        	TileImage tileImage = new TileImage();
            tileImage.setFileName(file.getName());
            tileImage.setFileType(Util.getFileExtension(file.getName()));
            tileImage.setFileSize(file.length());
            tileImage.setImage(Util.fileToByteArray(file));
            
            new MapDao().save(tileImage);
            
            addTileImageSelection(tileImage);
        }
	}
	
	@FXML
	private void saveButtonClicked(){
		if(map.getStartPoint() == null || map.getEndPoint() == null){
			new AlertDialog(AlertType.ERROR, "Invalid Map", null, "Map should have 1 start point and 1 end point.",stage).showAndWait();
		}else{
			new MapPropertiesController().show(currentAction, map);
			stage.close();
		}
	}
	
	@FXML
	private void cancelButtonClicked(){
		stage.close();
	}
	
	public Map getMap() {
		return map;
	}
	
	public void setMap(Map map) {
		this.map = map;
	}
	
	public void setStage(Stage stage) {
		this.stage = stage;
	}
	
	public void setViewTitle(String title){
		viewTitle.setText(title);
	}
	
	public Action getCurrentAction() {
		return currentAction;
	}
	
	public void setCurrentAction(Action currentAction) {
		this.currentAction = currentAction;
	}
}
