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
import com.creatingskies.game.core.resources.GameResourcesManager;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.util.Util;

public class GameCoreController extends PropertiesViewController {
	
	private final static Integer mainScreenHeight = Constant.TILE_HEIGHT * 5;
	private final static Integer mainScreenWidth = Constant.TILE_WIDTH * 5;
	private final static Integer miniScreenHeight = Constant.TILE_HEIGHT / 3;
	private final static Integer miniScreenWidth = Constant.TILE_WIDTH / 3;
	
	
	@FXML private Pane pane;
	@FXML private GridPane mapTiles;
	@FXML private ScrollPane mapScroller;
	@FXML private GridPane miniMapTiles;
	@FXML private Pane miniMapPane;
	
	@FXML private Label durationLabel;
	@FXML private Label countDownValue;
	@FXML private Label difficultyLabel;
	
	@FXML private ImageView warningImageView;
	@FXML private ImageView stopImageView;
	
	private List<Shape> obstacles;
	private List<Shape> obstacleEdges;
	
	private List<Shape> miniobstacles;
	private List<Shape> miniobstacleEdges;
	
	private Rectangle playingArea;
	private Rectangle endTile;
	private Rectangle miniplayingArea;
	private Rectangle miniendTile;
	
	private Double speedFactor = 0.5;
	private Double weatherSlowFactor = 0.0;
	private Double obstacleSlowFactor = 0.025;
	private Double totalObstacleSlowFactor = 0.0;
	private Double degreesInterval = 3.0;
	private Double degreesPreferred = 0.0;
	private Double maxPow = 3.0;
	private Double minPow = 1.0;
	
	private StackPane player;
	private StackPane miniplayer;
	private Circle playerCircle;
	private Circle miniplayerCircle;
	
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
		
		loadPlayer();
		initTimeline();
		initCountdownTimer();
		
		initMap(map);
		initWarningImages(map.getWidth(), map.getHeight());
		
		centerNode(stopImageView);
		centerNode(warningImageView);
		centerNode(countDownValue);
		
		
		inputReader.init();
		gameResourceManager = new GameResourcesManager(((GameEvent) getCurrentRecord()).getGame());
		
		mapScroller.setHmax(mapScroller.getContent().getBoundsInLocal().getWidth());
		mapScroller.setVmax(mapScroller.getContent().getBoundsInLocal().getHeight());
		
		countDownTimer.play();
	}
	
	private void initWarningImages(Integer width, Integer height){
		double centerX = ((width / 2) * Constant.TILE_WIDTH) - (warningImageView.getFitWidth() / 2);
		double centerY = ((height / 2) * Constant.TILE_HEIGHT) - (warningImageView.getFitHeight() / 2);
		
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
		miniobstacleEdges = new ArrayList<Shape>();
		miniobstacles = new ArrayList<Shape>();
		mapTiles.getChildren().clear();
		miniMapTiles.getChildren().clear();
		
		for(Tile tile : map.getTiles()){
			ImageView backImage = new ImageView();
			ImageView minibackImage = new ImageView();
			
			backImage.setFitHeight(mainScreenHeight);
			backImage.setFitWidth(mainScreenWidth);
			backImage.setImage(Util.byteArrayToImage(tile.getBackImage() != null ?
					tile.getBackImage().getImage() : map.getDefaultTileImage().getImage()));
			
			
			minibackImage.setFitHeight(miniScreenHeight);
			minibackImage.setFitWidth(miniScreenWidth);
			minibackImage.setImage(Util.byteArrayToImage(tile.getBackImage() != null ?
					tile.getBackImage().getImage() : map.getDefaultTileImage().getImage()));
			
			Group group = new Group(backImage);
			Group minigroup = new Group(minibackImage);
			
			if(tile.getObstacle() != null || tile.getStartPoint() || tile.getEndPoint()){
				ImageView frontImage = new ImageView();
				ImageView minifrontImage = new ImageView();
				frontImage.setFitHeight(mainScreenHeight);
				frontImage.setFitWidth(mainScreenWidth);
				frontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				
				minifrontImage.setFitHeight(miniScreenHeight);
				minifrontImage.setFitWidth(miniScreenWidth);
				minifrontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				
				group.getChildren().add(frontImage);
				minigroup.getChildren().add(minifrontImage);
				
				if(tile.getObstacle() != null){
					createObstacle(tile);
				} else if(tile.getStartPoint()){
					player.setLayoutX(tile.getColIndex() * mainScreenWidth);
					player.setLayoutY(tile.getRowIndex() * mainScreenHeight);
					pane.getChildren().add(player);
					
					miniplayer.setLayoutX(tile.getColIndex() * miniScreenWidth);
					miniplayer.setLayoutY(tile.getRowIndex() * miniScreenHeight);
					miniMapPane.getChildren().add(miniplayer);
				} else if(tile.getEndPoint()){
					createEndRectangle(tile);
				}
			}
			mapTiles.add(group, tile.getColIndex(), tile.getRowIndex());
			miniMapTiles.add(minigroup, tile.getColIndex(), tile.getRowIndex());
		}
		
		initPlayingArea(map);
	}
	
	private void initPlayingArea(Map map){
		playingArea = new Rectangle();
		playingArea.setWidth(mainScreenWidth * map.getWidth());
		playingArea.setHeight(mainScreenHeight * map.getHeight());
		playingArea.setFill(Color.TRANSPARENT);
		pane.getChildren().add(playingArea);
		
		miniplayingArea = new Rectangle();
		miniplayingArea.setWidth(miniScreenWidth * map.getWidth());
		miniplayingArea.setHeight(miniScreenHeight * map.getHeight());
		miniplayingArea.setFill(Color.TRANSPARENT);
		miniMapPane.getChildren().add(miniplayingArea);
	}
	
	@FXML
	private void handleReset(){
		millisGameDuration = 0;
		degreesPreferred = 0.0;
		player.setRotate(degreesPreferred);
		gameLoop.play();
		gameResourceManager.start();
	}
	
	private void initTimeline() {
		final float frameDuration = 50;
		gameLoop = new Timeline(new KeyFrame(Duration.millis(frameDuration),
				new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	millisGameDuration += frameDuration;
		    	float result = millisGameDuration / 1000.0f;
		    	durationLabel.setText(String.format("%.1f", result));
		    	
		    	inputForce = inputReader.readInput();
				computeMovement();
				computeMiniMapMovement();
		    }
		}));
		gameLoop.setCycleCount(Timeline.INDEFINITE);
	}
	
	private void computeMovement() {
		Double currentDeg = player.getRotate();
		Double deltaDeg = ((45/(maxPow - minPow)) * (inputForce.left - inputForce.right));
		
		degreesPreferred = currentDeg + deltaDeg;
		
		if(currentDeg.compareTo(degreesPreferred) != 0){
			int multiplier = currentDeg.compareTo(degreesPreferred) < 0 ? 1 : -1;
			
			Double rotation = Math.abs(deltaDeg) > degreesInterval ?
					degreesInterval * multiplier : deltaDeg * multiplier;
			currentDeg += rotation; 
		}
		player.setRotate(currentDeg);

		boolean encounteredBlockage = false;
		
		double oldX = player.getLayoutX();
		double oldY = player.getLayoutY();
		if(inputForce.left != 0 && inputForce.right != 0){
			double speed = (inputForce.left + inputForce.right)
					* Math.max((speedFactor - (weatherSlowFactor + totalObstacleSlowFactor)), 0.1);
			
			double cosValue = (speed * Math.cos(Math.toRadians(currentDeg)));
			double sinValue = (speed * Math.sin(Math.toRadians(currentDeg)));
			player.setLayoutX(player.getLayoutX() + cosValue);
			player.setLayoutY(player.getLayoutY() + sinValue);
			
			if(checkCollision(playerCircle)){
				encounteredBlockage = true;
				player.setLayoutX(player.getLayoutX() - cosValue);
				player.setLayoutY(player.getLayoutY() - sinValue);
			}
			
			if(player.getLayoutX() > oldX){
				mapScroller.setHvalue(player.getLayoutX());
			}else{
				mapScroller.setHvalue(player.getLayoutX());
			}
			if(player.getLayoutY() > oldY){
				mapScroller.setVvalue(player.getLayoutY());
			}else{
				mapScroller.setVvalue(player.getLayoutY());
			}
		}
		
		centerNode(stopImageView);
		centerNode(warningImageView);
		
		stopImageView.setVisible(encounteredBlockage);
		checkWarning(playerCircle, encounteredBlockage);
		checkGameStatus(playerCircle);
	}
	
	private void computeMiniMapMovement() {
		Double currentDeg = miniplayer.getRotate();
		Double deltaDeg = ((45/(maxPow - minPow)) * (inputForce.left - inputForce.right));
		
		Double degreesPreferred = currentDeg + deltaDeg;
		Double degreesInterval = 3.0;
		
		if(currentDeg.compareTo(degreesPreferred) != 0){
			int multiplier = currentDeg.compareTo(degreesPreferred) < 0 ? 1 : -1;
			
			Double rotation = Math.abs(deltaDeg) > degreesInterval ?
					degreesInterval * multiplier : deltaDeg * multiplier;
			currentDeg += rotation; 
		}
		miniplayer.setRotate(currentDeg);
		
		if(inputForce.left != 0 && inputForce.right != 0){
			double speed = ((inputForce.left + inputForce.right)
					* Math.max((speedFactor - (weatherSlowFactor + totalObstacleSlowFactor)), 0.1))/3;
			
			double cosValue = (speed * Math.cos(Math.toRadians(currentDeg))) / 3;
			double sinValue = (speed * Math.sin(Math.toRadians(currentDeg))) / 3;
			miniplayer.setLayoutX(miniplayer.getLayoutX() + cosValue);
			miniplayer.setLayoutY(miniplayer.getLayoutY() + sinValue);
			
			if(checkMiniMapCollision(miniplayerCircle)){
				miniplayer.setLayoutX(miniplayer.getLayoutX() - cosValue);
				miniplayer.setLayoutY(miniplayer.getLayoutY() - sinValue);
			}
			
		}
		
	}
	
	private void centerNode(Node node){
		double centerX = (mapScroller.getHvalue() + mapScroller.getBoundsInLocal().getWidth()) / 2;
		double centerY = (mapScroller.getVvalue() + mapScroller.getBoundsInLocal().getHeight()) / 2;
		node.setLayoutX(centerX);
		node.setLayoutY(centerY);
	}
	
	private void checkWarning(Shape block, boolean encounteredBlockage){
		Boolean hasCollision = false;
		Integer totalDifficulty = 0;
		
		for (Shape edge : obstacleEdges) {
			Shape intersect = Shape.intersect(block, edge);
			if (intersect.getBoundsInLocal().getWidth() != -1) {
				hasCollision = true;
				edge.setFill(Color.DODGERBLUE);
				totalDifficulty += edge.getUserData() != null ?
						Integer.valueOf(String.valueOf(edge.getUserData())) : 0;
			} else {
				edge.setFill(Color.TRANSPARENT);
			}
		}
		
		warningImageView.setVisible(hasCollision && !encounteredBlockage);
		totalObstacleSlowFactor = (hasCollision ? obstacleSlowFactor : 0.0) * totalDifficulty;
		difficultyLabel.setText(String.format("%.2f", totalObstacleSlowFactor));
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
	
	private boolean checkMiniMapCollision(Shape block) {
		boolean hasCollision = false;
		for (Shape o : miniobstacles) {
			Shape intersect = Shape.intersect(block, o);
			if (intersect.getBoundsInLocal().getWidth() != -1) {
				hasCollision = true;		
				break;
			}
		}
		
		if(!hasCollision){
			Shape outside = Shape.subtract(block, miniplayingArea);
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
	
	public void createObstacle(Tile tile){
		Rectangle obstacle = createDefaultRectangle(tile);
		Rectangle miniobstacle = createMiniRectangle(tile);
		
		pane.getChildren().add(obstacle);
		miniMapPane.getChildren().add(miniobstacle);
		
		obstacles.add(obstacle);
		miniobstacles.add(miniobstacle);
		createObstacleEdge(tile);
	}
	
	public void createObstacleEdge(Tile tile){
		Circle obstacleEdge = new Circle();
		obstacleEdge.setRadius(mainScreenWidth * tile.getObstacle().getRadius());
		obstacleEdge.setUserData(tile.getObstacle().getDifficulty());
		
		obstacleEdge.setLayoutX(tile.getColIndex() * mainScreenWidth + (mainScreenWidth / 2));
		obstacleEdge.setLayoutY(tile.getRowIndex() * mainScreenHeight + (mainScreenWidth / 2));

		obstacleEdge.setFill(Color.TRANSPARENT);
		obstacleEdge.setOpacity(0.20);
		
		pane.getChildren().add(obstacleEdge);
		obstacleEdges.add(obstacleEdge);
		
		Circle miniobstacleEdge = new Circle();
		miniobstacleEdge.setRadius(miniScreenWidth * tile.getObstacle().getRadius());
		miniobstacleEdge.setUserData(tile.getObstacle().getDifficulty());
		
		miniobstacleEdge.setLayoutX(tile.getColIndex() * miniScreenWidth + (miniScreenWidth / 2));
		miniobstacleEdge.setLayoutY(tile.getRowIndex() * miniScreenHeight + (miniScreenWidth / 2));

		miniobstacleEdge.setFill(Color.TRANSPARENT);
		miniobstacleEdge.setOpacity(0.20);
		
		miniMapPane.getChildren().add(miniobstacleEdge);
		miniobstacleEdges.add(miniobstacleEdge);
	}
	
	public void createEndRectangle(Tile tile){
		endTile = createDefaultRectangle(tile);
		pane.getChildren().add(endTile);
		
		miniendTile = createMiniRectangle(tile);
		miniMapPane.getChildren().add(miniendTile);
	}
	
	public Rectangle createDefaultRectangle(Tile tile){
		Rectangle rect = new Rectangle();
		rect.setWidth(mainScreenWidth);
		rect.setHeight(mainScreenHeight);
		rect.setFill(Color.TRANSPARENT);
		rect.setLayoutX(tile.getColIndex() * mainScreenWidth);
		rect.setLayoutY(tile.getRowIndex() * mainScreenHeight);
		return rect;
	}
	
	public Rectangle createMiniRectangle(Tile tile){
		Rectangle rect = new Rectangle();
		rect.setWidth(miniScreenWidth);
		rect.setHeight(miniScreenHeight);
		rect.setFill(Color.TRANSPARENT);
		rect.setLayoutX(tile.getColIndex() * miniScreenWidth);
		rect.setLayoutY(tile.getRowIndex() * miniScreenHeight);
		return rect;
	}
	
	private void loadPlayer(){
		playerCircle = new Circle((mainScreenWidth/2), Color.BLACK);
		playerCircle.setOpacity(0.2);
		
		ImageView playerImageView = new ImageView();
		playerImageView.setFitWidth(mainScreenWidth * 0.8);
		playerImageView.setFitHeight(mainScreenHeight * 0.8);
		playerImageView.setImage(new Image("/images/cyclist.png"));
		
		player = new StackPane();
		player.setPrefWidth(mainScreenWidth);
		player.setPrefHeight(mainScreenHeight);
		player.getChildren().addAll(playerCircle, playerImageView);
		
		
		miniplayerCircle = new Circle((miniScreenWidth/2), Color.BLACK);
		miniplayerCircle.setOpacity(0.2);
		
		ImageView miniplayerImageView = new ImageView();
		miniplayerImageView.setFitWidth(miniScreenWidth * 0.8);
		miniplayerImageView.setFitHeight(miniScreenHeight * 0.8);
		miniplayerImageView.setImage(new Image("/images/cyclist.png"));
		
		miniplayer = new StackPane();
		miniplayer.setPrefWidth(miniScreenWidth);
		miniplayer.setPrefHeight(miniScreenHeight);
		miniplayer.getChildren().addAll(miniplayerCircle, miniplayerImageView);
	}
	
}