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
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Label;
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
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.util.Util;

public class GameCoreController extends PropertiesViewController {
	
	@FXML private Pane pane;
	@FXML private GridPane mapTiles;
	
	@FXML private Label durationLabel;
	@FXML private Label countDownLabel;
	@FXML private Label difficultyLabel;
	
	@FXML private ImageView warningImageView;
	@FXML private ImageView stopImageView;
	
	private List<Shape> obstacles;
	private List<Shape> obstacleEdges;
	
	private Rectangle playingArea;
	private Rectangle endTile;
	
	private Double speedFactor = 0.5;
	private Double weatherSlowFactor = 0.0;
	private Double obstacleSlowFactor = 0.0;
	private Double degreesInterval = 3.0;
	private Double degreesPreferred = 0.0;
	private Double maxPow = 3.0;
	private Double minPow = 1.0;
	
	private StackPane player;
	private Circle playerCircle;
	
	private Map map;
	private Timeline gameLoop;
	private Timeline countDownTimer;
	
	private long millisGameDuration;
	private int countDown = 3;
	
	private AbstractInputReader inputReader;
	private InputForce inputForce;
	
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
		map = mapDao.findMapWithDetails(getGameEvent().getGame().getMap().getIdNo());
		
		loadPlayer();
		initMapTiles();
		initTimeline();
		initCountdownTimer();
		initWarningImages();
		
		inputReader.init();
	}
	
	private void initWarningImages(){
		warningImageView.setImage(new Image("/images/warning.png"));
		stopImageView.setImage(new Image("/images/stop.png"));
	}
	
	private void initCountdownTimer(){
		countDownTimer = new Timeline();
		countDownTimer.setCycleCount(Timeline.INDEFINITE);
		countDownTimer.getKeyFrames().add(new KeyFrame(Duration.millis(800),
			new EventHandler<ActionEvent>() {
				@Override
				public void handle(ActionEvent event) {
					countDownLabel.setText(String.valueOf(countDown));
					countDown--;
					
					if(countDown < 0){
						countDownLabel.setVisible(false);
						countDownTimer.stop();
						handleReset();
					}
				}
			}
		));
	}
	
	@FXML
	private void handleGameStart(){
        countDownTimer.play();
	}
	
	private void initMapTiles(){
		obstacleEdges = new ArrayList<Shape>();
		obstacles = new ArrayList<Shape>();
		mapTiles.getChildren().clear();
		
		for(Tile tile : map.getTiles()){
			ImageView backImage = new ImageView();
			backImage.setFitHeight(Constant.TILE_HEIGHT);
			backImage.setFitWidth(Constant.TILE_WIDTH);
			backImage.setImage(Util.byteArrayToImage(tile.getBackImage() != null ?
					tile.getBackImage().getImage() : map.getDefaultTileImage().getImage()));
			
			Group group = new Group(backImage);
			
			if(tile.getObstacle() != null || tile.getStartPoint() || tile.getEndPoint()){
				ImageView frontImage = new ImageView();
				frontImage.setFitHeight(Constant.TILE_HEIGHT);
				frontImage.setFitWidth(Constant.TILE_WIDTH);
				frontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				group.getChildren().add(frontImage);
				
				if(tile.getObstacle() != null){
					createObstacle(tile);
				} else if(tile.getStartPoint()){
					player.setLayoutX(tile.getColIndex() * Constant.TILE_WIDTH);
					player.setLayoutY(tile.getRowIndex() * Constant.TILE_HEIGHT);
					pane.getChildren().add(player);
				} else if(tile.getEndPoint()){
					createEndRectangle(tile);
				}
			}
			mapTiles.add(group, tile.getColIndex(), tile.getRowIndex());
		}
		
		initPlayingArea();
	}
	
	private void initPlayingArea(){
		playingArea = new Rectangle();
		playingArea.setWidth(Constant.TILE_WIDTH * map.getWidth());
		playingArea.setHeight(Constant.TILE_HEIGHT * map.getHeight());
		playingArea.setFill(Color.TRANSPARENT);
		pane.getChildren().add(playingArea);
	}
	
	@FXML
	private void handleReset(){
		millisGameDuration = 0;
		degreesPreferred = 0.0;
		player.setRotate(degreesPreferred);
		gameLoop.play();
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

		checkWarning(playerCircle);
		checkGameStatus(playerCircle);
		
		if(inputForce.left != 0 && inputForce.right != 0){
			double speed = (inputForce.left + inputForce.right)
					* Math.max((speedFactor - (weatherSlowFactor + obstacleSlowFactor)), 0);
			
			double cosValue = (speed * Math.cos(Math.toRadians(currentDeg)));
			double sinValue = (speed * Math.sin(Math.toRadians(currentDeg)));
			player.setLayoutX(player.getLayoutX() + cosValue);
			player.setLayoutY(player.getLayoutY() + sinValue);
			
			if(checkCollision(playerCircle)){
				player.setLayoutX(player.getLayoutX() - cosValue);
				player.setLayoutY(player.getLayoutY() - sinValue);
			}
		}
	}
	
	private void checkWarning(Shape block){
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
		
		warningImageView.setVisible(hasCollision);
		obstacleSlowFactor = (hasCollision ? 0.05 : 0.0) * totalDifficulty;
		difficultyLabel.setText(String.format("%.2f", obstacleSlowFactor));
	}
	
	private void checkGameStatus(Shape block){
		Shape intersect = Shape.intersect(block, endTile);
		if (intersect.getBoundsInLocal().getWidth() != -1) {
			gameLoop.stop();
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
		
		stopImageView.setVisible(hasCollision);
		return hasCollision;
	}
	
	@Override
	protected void close() {
		map = null;
		gameLoop = null;
		mapTiles.getChildren().clear();
		obstacles.clear();
		obstacleEdges.clear();
		super.close();
	}
	
	public void createObstacle(Tile tile){
		Rectangle obstacle = createDefaultRectangle(tile);
		pane.getChildren().add(obstacle);
		obstacles.add(obstacle);
		createObstacleEdge(tile);
	}
	
	public void createObstacleEdge(Tile tile){
		Circle obstacleEdge = new Circle();
		obstacleEdge.setRadius(Constant.TILE_WIDTH * tile.getObstacle().getRadius());
		obstacleEdge.setUserData(tile.getObstacle().getDifficulty());
		
		obstacleEdge.setLayoutX(tile.getColIndex() * Constant.TILE_WIDTH + (Constant.TILE_WIDTH / 2));
		obstacleEdge.setLayoutY(tile.getRowIndex() * Constant.TILE_HEIGHT + (Constant.TILE_WIDTH / 2));

		obstacleEdge.setFill(Color.TRANSPARENT);
		obstacleEdge.setOpacity(0.20);
		
		pane.getChildren().add(obstacleEdge);
		obstacleEdges.add(obstacleEdge);
	}
	
	public void createEndRectangle(Tile tile){
		endTile = createDefaultRectangle(tile);
		pane.getChildren().add(endTile);
	}
	
	public Rectangle createDefaultRectangle(Tile tile){
		Rectangle rect = new Rectangle();
		rect.setWidth(Constant.TILE_WIDTH);
		rect.setHeight(Constant.TILE_HEIGHT);
		rect.setFill(Color.TRANSPARENT);
		rect.setLayoutX(tile.getColIndex() * Constant.TILE_WIDTH);
		rect.setLayoutY(tile.getRowIndex() * Constant.TILE_HEIGHT);
		return rect;
	}
	
	private void loadPlayer(){
		playerCircle = new Circle((Constant.TILE_WIDTH/2), Color.BLACK);
		playerCircle.setOpacity(0.2);
		
		ImageView playerImageView = new ImageView();
		playerImageView.setFitWidth(Constant.TILE_WIDTH * 0.8);
		playerImageView.setFitHeight(Constant.TILE_HEIGHT * 0.8);
		playerImageView.setImage(new Image("/images/cyclist.png"));
		
		player = new StackPane();
		player.setPrefWidth(Constant.TILE_WIDTH);
		player.setPrefHeight(Constant.TILE_HEIGHT);
		player.getChildren().addAll(playerCircle, playerImageView);
	}
	
}