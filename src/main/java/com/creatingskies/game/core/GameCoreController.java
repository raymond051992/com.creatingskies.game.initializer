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
import javafx.scene.Node;
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
import k8055.K8055JavaCall;

import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.classes.Util;
import com.creatingskies.game.common.AlertDialog;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.config.event.GameEventTableViewController;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.event.GameEvent;

public class GameCoreController extends PropertiesViewController {
	
	@FXML private Pane pane;
	@FXML private GridPane mapTiles;
	
	@FXML private Label durationLabel;
	@FXML private Label countDownLabel;
	
	@FXML private ImageView warningImageView;
	@FXML private ImageView stopImageView;

	private List<Shape> obstacles;
	private List<Shape> obstacleEdges;
	
	private Rectangle playingArea;
	private Rectangle endTile;
	
	private Double speedFactor = 0.5, weatherSlowFactor = 0.0, obstacleSlowFactor = 0.0;
	private Double maxPow = 3.0, minPow = 1.0;
	private Integer leftPow = 0, rightPow = 0;
	
	private Double rotationInterval = 3.0;
	
	private Double preferredDeg = 0.0;
	private Timeline timeline;
	
	private StackPane playerStackPane;
	private ImageView playerImageView;
	private Circle playerCircle;
	private Node playerNode;
	
	private Map map;
	private K8055JavaCall k8055 = new K8055JavaCall();
	
	private Timeline countDownTimer;
	private int countDown = 3;
	private long millisGameDuration;
	
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
		super.init();

		MapDao mapDao = new MapDao();
		map = mapDao.findMapWithDetails(getGameEvent().getGame().getMap().getIdNo());
		
		initDevice();
		loadPlayer();
		initMapTiles();
		initTimeline();
		initCountdownTimer();
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
			ImageView imageView = new ImageView();
			imageView.setFitHeight(Constant.TILE_HEIGHT);
			imageView.setFitWidth(Constant.TILE_WIDTH);
			imageView.setImage(Util.byteArrayToImage(tile.getImage()));
			
			Pane tilePane = new Pane(imageView);
			tilePane.getStyleClass().add("map-designer-tile");
			mapTiles.add(tilePane, tile.getColIndex(), tile.getRowIndex());
			
			if(tile.getObstacleDifficulty() != null){
				createObstacle(tile);
			} else if(tile.getStartPoint()){
				playerNode.setLayoutX(tile.getColIndex() * Constant.TILE_WIDTH);
				playerNode.setLayoutY(tile.getRowIndex() * Constant.TILE_HEIGHT);
				pane.getChildren().add(playerNode);
			} else if(tile.getEndPoint()){
				createEndRectangle(tile);
			}
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
		leftPow = 0;
		rightPow = 0;
		preferredDeg = 0.0;
		playerNode.setRotate(preferredDeg);
		timeline.play();
	}
	
	private void initTimeline() {
		final float frameDuration = 50;
		timeline = new Timeline(new KeyFrame(Duration.millis(frameDuration),
				new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	millisGameDuration += frameDuration;
		    	float result = millisGameDuration / 1000.0f;
		    	durationLabel.setText(String.format("%.1f", result));
		    	
		    	readFromDevice();
				computeMovement();
		    }
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
	}
	
	private void computeMovement() {
		Double currentDeg = playerNode.getRotate();
		Double deltaDeg = ((45/(maxPow - minPow)) * (leftPow - rightPow));
		
		preferredDeg = currentDeg + deltaDeg;
		
		if(currentDeg.compareTo(preferredDeg) != 0){
			int multiplier = currentDeg.compareTo(preferredDeg) < 0 ? 1 : -1;
			
			Double rotation = Math.abs(deltaDeg) > rotationInterval ?
					rotationInterval * multiplier : deltaDeg * multiplier;
			currentDeg += rotation; 
		}
		playerNode.setRotate(currentDeg);
		
		if(rightPow != 0 && leftPow != 0){
			checkWarning(playerCircle);
			
			double speed = (leftPow + rightPow)
					* (speedFactor + weatherSlowFactor + obstacleSlowFactor);
			
			double cosValue = (speed * Math.cos(Math.toRadians(currentDeg)));
			double sinValue = (speed * Math.sin(Math.toRadians(currentDeg)));
			playerNode.setLayoutX(playerNode.getLayoutX() + cosValue);
			playerNode.setLayoutY(playerNode.getLayoutY() + sinValue);
			
			checkGameStatus(playerCircle);
			
			if(checkCollision(playerCircle)){
				playerNode.setLayoutX(playerNode.getLayoutX() - cosValue);
				playerNode.setLayoutY(playerNode.getLayoutY() - sinValue);
			}
		}
	}
	
	private void checkWarning(Shape block){
		boolean hasCollision = false;
		
		for (Shape o : obstacleEdges) {
			Shape intersect = Shape.intersect(block, o);
			if (intersect.getBoundsInLocal().getWidth() != -1) {
				hasCollision = true;		
				break;
			}
		}
		
		obstacleSlowFactor = hasCollision ? 0.5 : 0.0;
		warningImageView.setVisible(hasCollision);
	}
	
	private void checkGameStatus(Shape block){
		Shape intersect = Shape.intersect(block, endTile);
		if (intersect.getBoundsInLocal().getWidth() != -1) {
			timeline.stop();
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
		timeline = null;
		mapTiles.getChildren().clear();
		obstacles.clear();
		obstacleEdges.clear();
		k8055.CloseDevice();
		super.close();
	}

	public void readFromDevice() {
		try {
        	leftPow = 0;
        	rightPow = 0;
        	
        	if(k8055.ReadDigitalChannel(1) == 1) leftPow += 1;
        	if(k8055.ReadDigitalChannel(2) == 1) leftPow += 2;
        	
        	if(k8055.ReadDigitalChannel(4) == 1) rightPow += 1;
        	if(k8055.ReadDigitalChannel(5) == 1) rightPow += 2;
        } catch (Exception e){
        	e.printStackTrace();
        	k8055.CloseDevice();
        }
	}
	
	public void initDevice() {
		k8055.OpenDevice(0);
	}
	
	public void createObstacle(Tile tile){
		Rectangle obstacle = createDefaultRectangle(tile);
		pane.getChildren().add(obstacle);
		obstacles.add(obstacle);
		
		createObstacleEdge(tile);
	}
	
	public void createObstacleEdge(Tile tile){
		Rectangle obstacleEdge = new Rectangle();
		
		//Adjust pag maliit sa zero or lagpas sa boundary
		
		obstacleEdge.setWidth(Constant.TILE_WIDTH * ((tile.getObstacleDifficulty() * 2) + 1));
		obstacleEdge.setHeight(Constant.TILE_HEIGHT * ((tile.getObstacleDifficulty() * 2) + 1));
		obstacleEdge.setLayoutX((tile.getColIndex() - tile.getObstacleDifficulty()) * Constant.TILE_WIDTH);
		obstacleEdge.setLayoutY((tile.getRowIndex() - tile.getObstacleDifficulty()) * Constant.TILE_HEIGHT);
		
		obstacleEdge.setFill(Color.TRANSPARENT);
		//obstacleEdge.setOpacity(0.1);
		
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
		
		playerImageView = new ImageView();
		playerImageView.setFitWidth(Constant.TILE_WIDTH);
		playerImageView.setFitHeight(Constant.TILE_HEIGHT);
		playerImageView.setImage(new Image("/images/cyclist.png"));
		
		playerStackPane = new StackPane();
		playerStackPane.setPrefWidth(Constant.TILE_WIDTH);
		playerStackPane.setPrefHeight(Constant.TILE_HEIGHT);
		playerStackPane.getChildren().addAll(playerCircle, playerImageView);
		playerNode = playerStackPane;
	}
	
}
