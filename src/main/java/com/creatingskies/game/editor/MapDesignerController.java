package com.creatingskies.game.editor;

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
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;

import com.creatingskies.game.classes.Util;
import com.creatingskies.game.classes.ViewController.Action;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.Game;
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
	
	@FXML private Button saveButton;
	@FXML private Button cancelButton;

	private Obstacle selectedObstacle;
	private TileImage selectedTileImage;
	
	private ImageView startTileImageView;
	private ImageView endTileImageView;
	
	private boolean startTileSelected;
	private boolean endTileSelected;
	
	private Game game;
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
		
		for(Tile tile : getMap().getTiles()){
			ImageView imageView = new ImageView();
			imageView.setFitHeight(Constant.TILE_HEIGHT);
			imageView.setFitWidth(Constant.TILE_WIDTH);
			imageView.setImage(Util.byteArrayToImage(tile.getImage() != null ?
					tile.getImage().getImage() : getMap().getDefaultTileImage().getImage()));
			
			mapTiles.add(imageView, tile.getColIndex(), tile.getRowIndex());
			
			imageView.setOnMousePressed(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					handlePaintTile(tile, imageView);
				}
			});
			
			imageView.setOnMouseEntered(new EventHandler<MouseEvent>() {
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
		if(getCurrentAction() != null && (selectedTileImage != null || selectedObstacle != null)){
			tile.setObstacle(selectedObstacle != null ? selectedObstacle : null);
			tile.setImage(selectedTileImage != null ? selectedTileImage : null);
			
			if(selectedObstacle != null){
				imageView.setImage(Util.byteArrayToImage(selectedObstacle.getImage()));
			} else {
				imageView.setImage(Util.byteArrayToImage(tile.getImage() != null ?
						tile.getImage().getImage() : getMap().getDefaultTileImage().getImage()));
			}
			
			if(tile.getStartPoint()){
				tile.setStartPoint(false);
				requiredTileSelections.getChildren().add(startTileImageView);
			}
			
			if(tile.getEndPoint()){
				tile.setEndPoint(false);
				requiredTileSelections.getChildren().add(endTileImageView);
			}
			
			if(startTileSelected || endTileSelected){
				selectedTileImage = null;
				selectedTileImageView.setImage(null);
				tile.setStartPoint(startTileSelected);
				tile.setEndPoint(endTileSelected);
			}
			
			validateRequiredTiles();
		}
	}
	
	private void validateRequiredTiles(){
		if(getMap().getStartPoint() != null){
			requiredTileSelections.getChildren().remove(startTileImageView);
		}
		
		if(getMap().getEndPoint() != null){
			requiredTileSelections.getChildren().remove(endTileImageView);
		}
		
		requiredBox.setVisible(!requiredTileSelections.getChildren().isEmpty());
	}
	
	private void initTileImageSelections(){
		tileImageSelections.getChildren().clear();
		MapDao mapDao = new MapDao();
		List<TileImage> tileImages = mapDao.findAllTileImages(false);
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
		MapDao mapDao = new MapDao();
		TileImage startTileImage = mapDao.findTileImageByOwner(Constant.IMAGE_START_POINT_OWNER);
		
		startTileImageView = new ImageView(Util.byteArrayToImage(startTileImage.getImage()));
		startTileImageView.setFitHeight(Constant.TILE_HEIGHT);
		startTileImageView.setFitWidth(Constant.TILE_WIDTH);
		
		startTileImageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(startTileImageView.getImage());
				selectedTileImage = startTileImage;
				selectedObstacle = null;
				
				startTileSelected = true;
				endTileSelected = false;
			}
		});
		
		requiredTileSelections.getChildren().add(startTileImageView);
	}
	
	private void initEndTile(){
		MapDao mapDao = new MapDao();
		TileImage endTileImage = mapDao.findTileImageByOwner(Constant.IMAGE_END_POINT_OWNER);
		
		endTileImageView = new ImageView(Util.byteArrayToImage(endTileImage.getImage()));
		endTileImageView.setFitHeight(Constant.TILE_HEIGHT);
		endTileImageView.setFitWidth(Constant.TILE_WIDTH);
		
		endTileImageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(endTileImageView.getImage());
				selectedTileImage = endTileImage;
				selectedObstacle = null;
				
				startTileSelected = false;
				endTileSelected = true;
			}
		});
		
		requiredTileSelections.getChildren().add(endTileImageView);
	}
	
	private void addObstacleSelection(Obstacle obstacle){
		ImageView imageView = new ImageView(Util.byteArrayToImage(obstacle.getImage()));
		imageView.setFitHeight(Constant.TILE_HEIGHT);
		imageView.setFitWidth(Constant.TILE_WIDTH);
		
		imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(imageView.getImage());
				selectedTileImage = null;
				selectedObstacle = obstacle;
				
				startTileSelected = false;
				endTileSelected = false;
			}
		});
		
		obstacleImageSelections.getChildren().add(imageView);
	}
	
	private void addTileImageSelection(TileImage tileImage){
		ImageView imageView = new ImageView(Util.byteArrayToImage(tileImage.getImage()));
		imageView.setFitHeight(Constant.TILE_HEIGHT);
		imageView.setFitWidth(Constant.TILE_WIDTH);
		
		imageView.setOnMouseClicked(new EventHandler<MouseEvent>() {
			@Override
			public void handle(MouseEvent event) {
				selectedTileImageView.setImage(imageView.getImage());
				selectedTileImage = tileImage;
				selectedObstacle = null;
				
				startTileSelected = false;
				endTileSelected = false;
			}
		});
		
		tileImageSelections.getChildren().add(imageView);
	}
	
	public void show(Action action, Game game){
		try{
			FXMLLoader loader = new FXMLLoader();
	        loader.setLocation(getClass().getResource("Designer.fxml"));
	        AnchorPane designer = (AnchorPane) loader.load();
	        
	        designer.getStylesheets().add("/css/style.css");
	        stage = new Stage();
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        stage.initStyle(StageStyle.UNDECORATED);
	        Scene scene = new Scene(designer);
	        stage.setMaximized(true);
	        stage.setScene(scene);
	        
	        MapDesignerController controller = (MapDesignerController) loader.getController();
	        controller.setGame(game);
	        controller.setStage(stage);
	        
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
		if(getMap().getStartPoint() == null || getMap().getEndPoint() == null){
			new AlertDialog(AlertType.ERROR, "Invalid Map", null, "Map should have 1 start point and 1 end point.",stage).showAndWait();
		} else {
			new GamePropertiesController().show(currentAction, getGame());
			stage.close();
		}
	}
	
	@FXML
	private void cancelButtonClicked(){
		stage.close();
	}
	
	public Map getMap() {
		return getGame().getMap();
	}
	
	public Game getGame(){
		return game;
	}
	
	public void setGame(Game game){
		this.game = game;
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
