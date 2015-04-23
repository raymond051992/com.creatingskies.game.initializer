package com.creatingskies.game.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
import javafx.scene.control.ScrollPane;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;

import com.creatingskies.game.classes.AbstractInputReader;
import com.creatingskies.game.classes.AbstractInputReader.InputForce;
import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.config.event.GameEventTableViewController;
import com.creatingskies.game.core.Game.Type;
import com.creatingskies.game.core.resources.GameResourcesManager;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.util.Util;

public class GameCoreController extends PropertiesViewController {
	
	private static final Integer SCALE_FACTOR = 15;
	
	@FXML private Pane pane;
	@FXML private GridPane mapTiles;
	@FXML private ScrollPane mapScroller;
	@FXML private GridPane miniMapTiles;
	@FXML private Pane miniMapPane;
	
	@FXML private Label countDownValue;
	@FXML private Label durationLabel;
	@FXML private Label obstacleSlowLabel;
	@FXML private Label tileSlowLabel;
	
	@FXML private ImageView warningImageView;
	@FXML private ImageView stopImageView;
	@FXML private ImageView weatherImageView;
	
	private List<Shape> obstacles;
	private List<Shape> obstacleEdges;
	private List<Shape> tileShapes;
	
	private Rectangle playingArea;
	private Rectangle endTile;
	
	private Double weatherSlowFactor = 0.0;
	private Double obstacleSlowFactor = 0.0;
	private Double tileSlowFactor = 0.0;
	
	private Double degreesInterval = 3.0;
	private Double degreesPreferred = 0.0;
	private Double maxMovementSpeed = 7.0;
	
	private StackPane player;
	private StackPane miniPlayer;
	private Circle playerCircle;
	private Circle miniPlayerCircle;
	
	private Timeline gameLoop;
	private Timeline countDownTimer;
	
	private long millisGameDuration;
	private int countDown = 3;
	
	private AbstractInputReader inputReader;
	private InputForce inputForce;
	
	private GameResourcesManager gameResourceManager;
	
	@Override
	protected String getViewTitle() {
		return "Game";
	}

	public void show(GameEvent gameEvent) {
		try {
			FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("GameCore.fxml"));
            AnchorPane event = (AnchorPane) loader.load();
            
            GameCoreController controller = (GameCoreController) loader.getController();
            controller.setCurrentAction(Action.VIEW);
            controller.setCurrentRecord(gameEvent);
            controller.init();
            
            MainLayout.getRootLayout().setCenter(event);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private GameEvent getGameEvent(){
		return (GameEvent) getCurrentRecord();
	}
	
	public void init() {
		inputReader = new KeyboardInputReader();
		
		super.init();
		MapDao mapDao = new MapDao();
		Map map = mapDao.findMapWithDetails(getGameEvent().getGame().getMap().getIdNo());
		
		initPlayer(getGameEvent().getGame().getType());
		initGameLoop();
		initCountdownTimer();
		
		initMap(map);
		initWarningImages(map.getWidth(), map.getHeight());
		initWeathers();
		
		inputReader.init();
		gameResourceManager = new GameResourcesManager(((GameEvent) getCurrentRecord()).getGame());
		
		mapScroller.setHmax(mapScroller.getContent().getBoundsInLocal().getWidth());
		mapScroller.setVmax(mapScroller.getContent().getBoundsInLocal().getHeight());
		
		//TODO Replace workaround
//		mapScroller.setHvalue(player.getLayoutX() - 200);
//		mapScroller.setVvalue(player.getLayoutY() + 400);
		
		centerNode(stopImageView);
		centerNode(warningImageView);
		centerNode(countDownValue);
		
		countDownTimer.play();
	}
	
	private void initWeathers(){
		if(getGameEvent().getGame().getWeather() != null){
			weatherImageView.setImage(Util.byteArrayToImage(getGameEvent().getGame().getWeather().getImage()));
		}
	}
	
	private void initWarningImages(Integer width, Integer height){
		double centerX = ((width / 2) * getMiniScreenTileWidth()) - (warningImageView.getFitWidth() / 2);
		double centerY = ((height / 2) * getMiniScreenTileHeight()) - (warningImageView.getFitHeight() / 2);
		
		warningImageView.setOpacity(0.5);
		stopImageView.setOpacity(0.5);
		
		warningImageView.setImage(new Image("/images/warning.png"));
		stopImageView.setImage(new Image("/images/stop.png"));
		
		warningImageView.setLayoutX(centerX);
		warningImageView.setLayoutY(centerY);
		warningImageView.toFront();
		
		stopImageView.setLayoutX(centerX);
		stopImageView.setLayoutY(centerY);
		stopImageView.toFront();
	}
	
	private void initCountdownTimer(){
		renderCountdown(false);
		countDownTimer = new Timeline();
		countDownTimer.setCycleCount(Timeline.INDEFINITE);
		countDownTimer.getKeyFrames().add(new KeyFrame(Duration.millis(800),
			new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					renderCountdown(true);
					countDownValue.setText(String.valueOf(countDown));
					countDown--;
					
					if(countDown < 0){
						renderCountdown(false);
						countDownTimer.stop();
						handleReset();
					}
				}
			}
		));
	}
	
	private void renderCountdown(boolean visible){
		countDownValue.setVisible(visible);
	}
	
	@FXML
	private void handleGameStart(){
        countDownTimer.play();
	}
	
	private void initMap(Map map){
		obstacleEdges = new ArrayList<Shape>();
		obstacles = new ArrayList<Shape>();
		tileShapes = new ArrayList<Shape>();
		mapTiles.getChildren().clear();
		miniMapTiles.getChildren().clear();
		
		for(Tile tile : map.getTiles()){
			ImageView backImage = new ImageView();
			ImageView minibackImage = new ImageView();
			minibackImage.setOpacity(0.5);
			
			backImage.setFitHeight(getMainScreenTileHeight());
			backImage.setFitWidth(getMainScreenTileWidth());
			backImage.setImage(Util.byteArrayToImage(tile.getBackImage() != null ?
					tile.getBackImage().getImage() : map.getDefaultTileImage().getImage()));
			
			
			minibackImage.setFitHeight(getMiniScreenTileHeight());
			minibackImage.setFitWidth(getMiniScreenTileWidth());
			minibackImage.setImage(Util.byteArrayToImage(tile.getBackImage() != null ?
					tile.getBackImage().getImage() : map.getDefaultTileImage().getImage()));
			
			Group group = new Group(backImage);
			Group minigroup = new Group(minibackImage);
			
			if(tile.getObstacle() != null || tile.getStartPoint() || tile.getEndPoint()){
				ImageView frontImage = new ImageView();
				ImageView minifrontImage = new ImageView();
				frontImage.setFitHeight(getMainScreenTileHeight());
				frontImage.setFitWidth(getMainScreenTileWidth());
				frontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				
				minifrontImage.setFitHeight(getMiniScreenTileHeight());
				minifrontImage.setFitWidth(getMiniScreenTileWidth());
				minifrontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				
				group.getChildren().add(frontImage);
				minigroup.getChildren().add(minifrontImage);
				
				if(tile.getObstacle() != null){
					createObstacle(tile);
				} else if(tile.getStartPoint()){
					player.setLayoutX(tile.getColIndex() * getMainScreenTileWidth());
					player.setLayoutY(tile.getRowIndex() * getMainScreenTileHeight());
					pane.getChildren().add(player);
					
					miniPlayer.setLayoutX(tile.getColIndex() * getMiniScreenTileWidth());
					miniPlayer.setLayoutY(tile.getRowIndex() * getMiniScreenTileHeight());
					miniMapPane.getChildren().add(miniPlayer);
				} else if(tile.getEndPoint()){
					createEndRectangle(tile);
				}
				
				createTileShapes(tile, map.getDefaultTileImage().getDifficulty());
			}
			mapTiles.add(group, tile.getColIndex(), tile.getRowIndex());
			miniMapTiles.add(minigroup, tile.getColIndex(), tile.getRowIndex());
		}
		
		initPlayingArea(map);
	}
	
	private void initPlayingArea(Map map){
		playingArea = new Rectangle();
		playingArea.setWidth(getMainScreenTileWidth() * map.getWidth());
		playingArea.setHeight(getMainScreenTileHeight() * map.getHeight());
		playingArea.setFill(Color.TRANSPARENT);
		pane.getChildren().add(playingArea);
	}
	
	@FXML
	private void handleReset(){
		millisGameDuration = 0;
		degreesPreferred = 0.0;
		player.setRotate(degreesPreferred);
		gameLoop.play();
		gameResourceManager.start();
	}
	
	private void initGameLoop() {
		final float frameDuration = 50;
		gameLoop = new Timeline(new KeyFrame(Duration.millis(frameDuration),
				new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	millisGameDuration += frameDuration;
		    	float result = millisGameDuration / 1000.0f;
		    	durationLabel.setText(String.format("%.1f", result));
		    	
		    	inputForce = inputReader.readInput();
		    	computeRotation();
				computeMovement();
		    }
		}));
		gameLoop.setCycleCount(Timeline.INDEFINITE);
	}
	
	private void computeRotation(){
		Double currentDeg = player.getRotate();
		Double deltaDeg = ((45 / maxMovementSpeed) * (inputForce.left - inputForce.right));
		
		degreesPreferred = currentDeg + deltaDeg;
		
		if(currentDeg.compareTo(degreesPreferred) != 0){
			int multiplier = currentDeg.compareTo(degreesPreferred) < 0 ? 1 : -1;
			
			Double rotation = Math.abs(deltaDeg) > degreesInterval ?
					degreesInterval * multiplier : deltaDeg * multiplier;
			currentDeg += rotation; 
		}
		player.setRotate(currentDeg);
		miniPlayer.setRotate(currentDeg);
	}
	
	private void computeMovement() {
		checkWarning(playerCircle);
		checkTileProperty(playerCircle);
		checkGameStatus(playerCircle);
		
		boolean encounteredBlockage = false;
		double totalSlowFactor = 0;
		double speed = 0;
		
		if(inputForce.left != 0 && inputForce.right != 0){
			totalSlowFactor = weatherSlowFactor + obstacleSlowFactor + tileSlowFactor;
			speed = Math.max((((inputForce.left + inputForce.right)
					/ (maxMovementSpeed * 2)) * maxMovementSpeed) - totalSlowFactor, 0.1);
			
			double cosValue = (speed * Math.cos(Math.toRadians(player.getRotate())));
			double sinValue = (speed * Math.sin(Math.toRadians(player.getRotate())));
			
			player.setLayoutX(player.getLayoutX() + cosValue);
			player.setLayoutY(player.getLayoutY() + sinValue);
			miniPlayer.setLayoutX(miniPlayer.getLayoutX() + (cosValue / SCALE_FACTOR));
			miniPlayer.setLayoutY(miniPlayer.getLayoutY() + (sinValue / SCALE_FACTOR));
			
			//TODO Replace workaround
			double multiplier = 1.4;
			mapScroller.setHvalue(mapScroller.getHvalue() + (cosValue * multiplier));
			mapScroller.setVvalue(mapScroller.getVvalue() + (sinValue * multiplier));
			
			if(checkCollision(playerCircle)){
				encounteredBlockage = true;
				player.setLayoutX(player.getLayoutX() - cosValue);
				player.setLayoutY(player.getLayoutY() - sinValue);
				miniPlayer.setLayoutX(miniPlayer.getLayoutX() - (cosValue / SCALE_FACTOR));
				miniPlayer.setLayoutY(miniPlayer.getLayoutY() - (sinValue / SCALE_FACTOR));
				
				mapScroller.setHvalue(mapScroller.getHvalue() - (cosValue * multiplier));
				mapScroller.setVvalue(mapScroller.getVvalue() - (sinValue * multiplier));
			}
		}
		
		centerNode(stopImageView);
		centerNode(warningImageView);
		
		inputReader.display(speed, totalSlowFactor, player.getRotate());
		warningImageView.setVisible(warningImageView.isVisible() && !encounteredBlockage);
		stopImageView.setVisible(encounteredBlockage);
		
	}
	
	private void centerNode(Node node){
		node.setLayoutX(player.getLayoutX());
		node.setLayoutY(player.getLayoutY());
	}
	
	private void checkWarning(Shape block){
		Boolean hasCollision = false;
		obstacleSlowFactor = 0.0;
		
		for (Shape edge : obstacleEdges) {
			Shape intersect = Shape.intersect(block, edge);
			if (intersect.getBoundsInLocal().getWidth() != -1) {
				hasCollision = true;
				edge.setFill(Color.DODGERBLUE);
				obstacleSlowFactor = (double) Math.max(edge.getUserData() != null ?
						Integer.valueOf(String.valueOf(edge.getUserData())) : 0, obstacleSlowFactor);
			} else {
				edge.setFill(Color.TRANSPARENT);
			}
		}
		
		warningImageView.setVisible(hasCollision);
		obstacleSlowLabel.setText(String.format("%.2f", obstacleSlowFactor));
	}
	
	private void checkTileProperty(Shape block){
		tileSlowFactor = 0.0;
		
		for (Shape tileShape : tileShapes) {
			Shape intersect = Shape.intersect(block, tileShape);
			if (intersect.getBoundsInLocal().getWidth() != -1) {
				tileSlowFactor = (double) Math.max(tileShape.getUserData() != null ?
						Integer.valueOf(String.valueOf(tileShape.getUserData())) : 0, tileSlowFactor);
			}
		}
		
		tileSlowLabel.setText(String.format("%.2f", tileSlowFactor));
	}
	
	private void checkGameStatus(Shape block){
		Shape intersect = Shape.intersect(block, endTile);
		if (intersect.getBoundsInLocal().getWidth() != -1) {
			gameLoop.stop();
			gameResourceManager.stop();
			float result = millisGameDuration / 1000.0f;
			new AlertDialog(AlertType.INFORMATION, "Finish!",  null,
					"Duration: " + String.format("%.1f", result)).showAndWait();
			close();
			new GameEventTableViewController().show();
		}
	}
	
	private boolean checkCollision(Shape block) {
		boolean hasCollision = false;
		for (Shape o : obstacles) {
			Shape intersect = Shape.intersect(block, o);
			if (intersect.getBoundsInLocal().getWidth() != -1) {
				hasCollision = true;		
				break;
			}
		}
		
		if(!hasCollision){
			Shape outside = Shape.subtract(block, playingArea);
			if (outside.getBoundsInLocal().getWidth() != -1) {
				hasCollision = true;
			}
		}
		
		return hasCollision;
	}
	
	@Override
	protected void close() {
		gameLoop = null;
		mapTiles.getChildren().clear();
		obstacles.clear();
		obstacleEdges.clear();
		gameResourceManager.stop();
		super.close();
	}
	
	public void createTileShapes(Tile tile, Integer defaultDifficulty){
		Rectangle tileShape = createDefaultRectangle(tile);
		tileShape.setUserData(tile.getBackImage() != null ?
			tile.getBackImage().getDifficulty() : defaultDifficulty);
		pane.getChildren().add(tileShape);
		tileShapes.add(tileShape);
	}
	
	public void createObstacle(Tile tile){
		Rectangle obstacle = createDefaultRectangle(tile);
		pane.getChildren().add(obstacle);
		obstacles.add(obstacle);
		createObstacleEdge(tile);
	}
	
	private void createObstacleEdge(Tile tile){
		Circle obstacleEdge = new Circle();
		obstacleEdge.setRadius(getMainScreenTileWidth() * tile.getObstacle().getRadius());
		obstacleEdge.setUserData(tile.getObstacle().getDifficulty());
		
		obstacleEdge.setLayoutX(tile.getColIndex() * getMainScreenTileWidth() + (getMainScreenTileWidth() / 2));
		obstacleEdge.setLayoutY(tile.getRowIndex() * getMainScreenTileHeight() + (getMainScreenTileWidth() / 2));

		obstacleEdge.setFill(Color.TRANSPARENT);
		obstacleEdge.setOpacity(0.20);
		
		pane.getChildren().add(obstacleEdge);
		obstacleEdges.add(obstacleEdge);
	}
	
	private void createEndRectangle(Tile tile){
		endTile = createDefaultRectangle(tile);
		pane.getChildren().add(endTile);
	}
	
	private Rectangle createDefaultRectangle(Tile tile){
		Rectangle rect = new Rectangle();
		rect.setWidth(getMainScreenTileWidth());
		rect.setHeight(getMainScreenTileHeight());
		rect.setFill(Color.TRANSPARENT);
		rect.setLayoutX(tile.getColIndex() * getMainScreenTileWidth());
		rect.setLayoutY(tile.getRowIndex() * getMainScreenTileHeight());
		return rect;
	}
	
	private void initPlayer(Type type){
		double playerResizeFactor = 0.8;
		
		String owner = type.equals(Type.ROWING) ? Constant.IMAGE_ROWING_PLAYER_OWNER : Constant.IMAGE_CYCLING_PLAYER_OWNER;
		TileImage playerTileImage = new MapDao().findTileImageByOwner(owner);
		Image playerImage = playerTileImage != null && playerTileImage.getImage() != null ?
				Util.byteArrayToImage(playerTileImage.getImage()) : null;
		
		playerCircle = new Circle((getMainScreenTileWidth() / 2) * playerResizeFactor, Color.BLACK);
		playerCircle.setOpacity(0.2);
		
		ImageView playerImageView = new ImageView();
		playerImageView.setFitWidth(getMainScreenTileWidth() * playerResizeFactor);
		playerImageView.setFitHeight(getMainScreenTileHeight() * playerResizeFactor);
		playerImageView.setImage(playerImage);
		
		player = new StackPane();
		player.setPrefWidth(getMainScreenTileWidth());
		player.setPrefHeight(getMainScreenTileHeight());
		player.getChildren().addAll(playerCircle, playerImageView);
		
		miniPlayerCircle = new Circle((getMiniScreenTileWidth() / 2) * playerResizeFactor, Color.BLACK);
		miniPlayerCircle.setOpacity(0.2);
		
		ImageView miniplayerImageView = new ImageView();
		miniplayerImageView.setFitWidth(getMiniScreenTileWidth() * playerResizeFactor);
		miniplayerImageView.setFitHeight(getMiniScreenTileHeight() * playerResizeFactor);
		miniplayerImageView.setImage(playerImage);
		
		miniPlayer = new StackPane();
		miniPlayer.setPrefWidth(getMiniScreenTileWidth());
		miniPlayer.setPrefHeight(getMiniScreenTileHeight());
		miniPlayer.getChildren().addAll(miniPlayerCircle, miniplayerImageView);
	}
	
	private double getMainScreenTileWidth(){
		return getMiniScreenTileWidth() * SCALE_FACTOR;
	}
	
	private double getMainScreenTileHeight(){
		return getMiniScreenTileHeight() * SCALE_FACTOR;
	}
	
	private double getMiniScreenTileWidth(){
		return 16.0;
	}
	
	private double getMiniScreenTileHeight(){
		return 16.0;
	}
	
}