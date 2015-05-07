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
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
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
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

import com.creatingskies.game.classes.AbstractInputReader;
import com.creatingskies.game.classes.AbstractInputReader.InputForce;
import com.creatingskies.game.classes.PropertiesViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.component.AlertDialog;
import com.creatingskies.game.core.Game.Type;
import com.creatingskies.game.core.resources.GameResourcesManager;
import com.creatingskies.game.model.Constant;
import com.creatingskies.game.model.company.Group;
import com.creatingskies.game.model.event.GameEvent;
import com.creatingskies.game.util.Util;

public class GameCoreController extends PropertiesViewController {
	
	private static final Integer SCALE_FACTOR = 15;

	@FXML private Pane pane;
	@FXML private AnchorPane mainContainer;
	@FXML private AnchorPane weatherContainer;
	@FXML private GridPane mapTiles;
	@FXML private GridPane miniMapTiles;
	@FXML private Pane miniMapPane;
	
	@FXML private Label countDownValue;
	@FXML private Label durationLabel;
	@FXML private Label distanceLabel;
	@FXML private Label speedLabel;
	
	@FXML private Label obstacleSlowLabel;
	@FXML private Label tileSlowLabel;
	@FXML private Label weatherSlowLabel;
	
	@FXML private Label mapWidthLabel;
	@FXML private Label mapHeightLabel;
	@FXML private Label screenWidthLabel;
	@FXML private Label screenHeightLabel;
	@FXML private Label scrollHvalLabel;
	@FXML private Label scrollVvalLabel;
	@FXML private Label playerXLabel;
	@FXML private Label playerYLabel;
	
	@FXML private ImageView warningImageView;
	@FXML private ImageView stopImageView;
	
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

	private float millisGameDuration;
	private float totalDistance = 0;
	private double distance;
	private int countDown = 3;
	
	private AbstractInputReader inputReader;
	private InputForce inputForce;
	
	private GameResourcesManager gameResourceManager;
	private Group group;
	private Stage stage;
	
	@Override
	protected String getViewTitle() {
		return "Game";
	}

	public void show(GameEvent gameEvent, Group group) {
		try {
			FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("GameCore.fxml"));
            MainLayout.setModalLayout(loader.load());
            
            stage = new Stage();
	        stage.initModality(Modality.WINDOW_MODAL);
	        stage.initOwner(MainLayout.getPrimaryStage());
	        stage.initStyle(StageStyle.UNDECORATED);
	        Scene scene = new Scene(MainLayout.getModalLayout());
	        scene.setCursor(Cursor.NONE);
	        scene.getStylesheets()
        	.add("/css/dialog.css");
	        stage.setMaximized(true);
	        stage.setScene(scene);
	        
            GameCoreController controller = (GameCoreController) loader.getController();
            controller.setCurrentAction(Action.VIEW);
            controller.setCurrentRecord(gameEvent);
	        controller.setStage(stage);
	        controller.setGroup(group);
	        controller.init();
	        stage.show();
	        
	        MainLayout.getModalLayout().requestFocus();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	private void showMouseCursor(){
		if(stage.getScene().getCursor() == Cursor.DEFAULT){
			stage.getScene().setCursor(Cursor.NONE);
		}else{
			stage.getScene().setCursor(Cursor.DEFAULT);
		}
	}
	
	@FXML 
	private void closeGame(){
		close();
		stage.close();
	}
	
	private GameEvent getGameEvent(){
		return (GameEvent) getCurrentRecord();
	}
	
	public void init() {
		inputReader = new KeyboardInputReader();
		
		super.init();
		MapDao mapDao = new MapDao();
		Map map = mapDao.findMapWithDetails(getGameEvent().getGame().getMap().getIdNo());
		
		gameResourceManager = new GameResourcesManager(((GameEvent) getCurrentRecord()).getGame(),weatherContainer);
		
		initPlayer(getGameEvent().getGame().getType());
		initGameLoop();
		initCountdownTimer();
		
		initMap(map);
		initWarningImages();
		initWeathers();
		
		inputReader.init();
		
		countDownTimer.play();
	}
	
	private String convertToString(Object s){
		return String.valueOf(s);
	}
	
	private void initWeathers(){
		if(getGameEvent().getGame().getWeather() != null){
			weatherSlowFactor = (double) getGameEvent().getGame().getWeather().getDifficulty();
			weatherSlowLabel.setText(String.format("%.2f", weatherSlowFactor));
		}
	}
	
	private void initWarningImages(){
		warningImageView.setOpacity(0.5);
		stopImageView.setOpacity(0.5);
		
		warningImageView.setImage(new Image("/images/warning.png"));
		stopImageView.setImage(new Image("/images/stop.png"));
		
		warningImageView.toFront();
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
					
					centerNode(countDownValue, countDownValue.getWidth(), countDownValue.getHeight());
		            centerNode(stopImageView, stopImageView.getFitWidth(), stopImageView.getFitHeight());
		    		centerNode(warningImageView, warningImageView.getFitWidth(), warningImageView.getFitHeight());
					
		    		scrollMap();
					
	    			mainContainer.setMaxSize(pane.getWidth(), pane.getHeight());
					mapWidthLabel.setText(convertToString(mapTiles.getWidth()));
					mapHeightLabel.setText(convertToString(mapTiles.getHeight()));
					
					screenWidthLabel.setText(convertToString(stage.getWidth()));
					screenHeightLabel.setText(convertToString(stage.getHeight()));
					
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
			ImageView miniBackImage = new ImageView();
			miniBackImage.setOpacity(0.5);
			
			backImage.setFitHeight(getMainScreenTileHeight());
			backImage.setFitWidth(getMainScreenTileWidth());
			backImage.setImage(Util.byteArrayToImage(tile.getBackImage() != null ?
					tile.getBackImage().getImage() : map.getDefaultTileImage().getImage()));
			
			miniBackImage.setFitHeight(getMiniScreenTileHeight());
			miniBackImage.setFitWidth(getMiniScreenTileWidth());
			miniBackImage.setImage(Util.byteArrayToImage(tile.getBackImage() != null ?
					tile.getBackImage().getImage() : map.getDefaultTileImage().getImage()));
			
			ImageView frontImage = null;
			ImageView miniFrontImage = null;
			
			if(tile.getObstacle() != null || tile.getStartPoint() || tile.getEndPoint()){
				frontImage = new ImageView();
				miniFrontImage = new ImageView();
				frontImage.setFitHeight(getMainScreenTileHeight());
				frontImage.setFitWidth(getMainScreenTileWidth());
				frontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				
				miniFrontImage.setFitHeight(getMiniScreenTileHeight());
				miniFrontImage.setFitWidth(getMiniScreenTileWidth());
				miniFrontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				
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
			
			mapTiles.add(Util.bundle(backImage, frontImage), tile.getColIndex(), tile.getRowIndex());
			miniMapTiles.add(Util.bundle(miniBackImage, miniFrontImage), tile.getColIndex(), tile.getRowIndex());
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
		gameLoop = new Timeline(new KeyFrame(Duration.millis(Constant.FRAME_DURATION),
				new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	millisGameDuration += Constant.FRAME_DURATION;
		    	durationLabel.setText(String.format("%.2f", getDuration()));
		    	distanceLabel.setText(String.format("%.2f", totalDistance));
		    	speedLabel.setText(String.format("%.2f", Util.computeSpeed(distance)) + " m/s");
		    	
		    	inputForce = inputReader.readInput();
		    	computeRotation();
				computeMovement();
				
				scrollHvalLabel.setText(convertToString(pane.getLayoutX()));
				scrollVvalLabel.setText(convertToString(pane.getLayoutY()));
				
				playerXLabel.setText(convertToString(player.getLayoutX()));
				playerYLabel.setText(convertToString(player.getLayoutY()));
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
		distance = 0;

		totalSlowFactor = weatherSlowFactor + obstacleSlowFactor + tileSlowFactor;
		
		if(inputForce.left != 0 && inputForce.right != 0){
			distance = Math.max((((inputForce.left + inputForce.right)
					/ (maxMovementSpeed * 2)) * maxMovementSpeed) - totalSlowFactor, 0.1);
			
			totalDistance += distance;
			
			double cosValue = (distance * Math.cos(Math.toRadians(player.getRotate())));
			double sinValue = (distance * Math.sin(Math.toRadians(player.getRotate())));
			
			player.setLayoutX(player.getLayoutX() + cosValue);
			player.setLayoutY(player.getLayoutY() + sinValue);
			miniPlayer.setLayoutX(miniPlayer.getLayoutX() + (cosValue / SCALE_FACTOR));
			miniPlayer.setLayoutY(miniPlayer.getLayoutY() + (sinValue / SCALE_FACTOR));
			
			scrollMap();
			
			if(checkCollision(playerCircle)){
				encounteredBlockage = true;
				player.setLayoutX(player.getLayoutX() - cosValue);
				player.setLayoutY(player.getLayoutY() - sinValue);
				miniPlayer.setLayoutX(miniPlayer.getLayoutX() - (cosValue / SCALE_FACTOR));
				miniPlayer.setLayoutY(miniPlayer.getLayoutY() - (sinValue / SCALE_FACTOR));
			}
		}
		
		inputReader.display(distance, totalSlowFactor, player.getRotate());
		warningImageView.setVisible(warningImageView.isVisible() && !encounteredBlockage);
		stopImageView.setVisible(encounteredBlockage);
		
	}
	
	private void scrollMap(){
		if(player.getLayoutX() > 0 && (player.getLayoutX() + ((stage.getWidth() + player.getWidth()) / 2)) < pane.getBoundsInLocal().getWidth()){
			if((0-(player.getLayoutX() - ((stage.getWidth() - player.getWidth()) / 2))) < 0){
				pane.setLayoutX(0-(player.getLayoutX() - ((stage.getWidth() - player.getWidth()) / 2)));
			}else{
				pane.setLayoutX(0-(player.getLayoutX()));
			}
		}
		
		if(player.getLayoutY() > 0 && (player.getLayoutY() + ((stage.getHeight() + player.getHeight()) / 2)) < pane.getBoundsInLocal().getHeight()){
			if((0-(player.getLayoutY() - ((stage.getHeight() - player.getHeight()) / 2))) < 0){
				pane.setLayoutY(0-(player.getLayoutY() - ((stage.getHeight() - player.getHeight()) / 2)));
			}else{
				pane.setLayoutY(0-(player.getLayoutY()));
			}
		}
	}
	
	private void centerNode(Node node, double width, double height){
		node.setLayoutX((stage.getWidth() - width) / 2);
		node.setLayoutY((stage.getHeight() - height) / 2);
	}
	
	private void checkWarning(Shape block){
		Integer intersectionIndex = null;
		obstacleSlowFactor = 0.0;
		
		for (Shape edge : obstacleEdges) {
			Shape intersect = Shape.intersect(block, edge);
			if (intersect.getBoundsInLocal().getWidth() != -1) {
				int edgeSlowFactor = edge.getUserData() != null ?
						Integer.valueOf(String.valueOf(edge.getUserData())) : 0;
						
				if(obstacleSlowFactor < edgeSlowFactor){
					obstacleSlowFactor = (double) edgeSlowFactor;
					intersectionIndex = obstacleEdges.indexOf(edge);
				}
			}
			edge.setFill(Color.TRANSPARENT);
		}
		
		if(intersectionIndex != null){
			obstacleEdges.get(intersectionIndex).setFill(Util.getDifficultyColor(obstacleSlowFactor));
		}
		
		warningImageView.setVisible(intersectionIndex != null);
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
			
			saveGameResult();
			new AlertDialog(AlertType.INFORMATION, "Finish!",  "Congratulations!",
					" Company: " + group.getCompany().getName()
					+ "\n Group: " + group.getName()
					+ "\n Duration: " + String.format("%.2f", getDuration())
					+ "\n Average Speed: " + String.format("%.2f", Util.computeSpeed(totalDistance, getDuration())))
					.showAndWait();

			close();
			stage.close();
		}
	}
	
	private float getDuration(){
		return millisGameDuration / 1000.0f;
	}
	
	private void saveGameResult(){
		GameResult result = new GameResult();
		result.setGame(getGameEvent().getGame());
		result.setDuration((double) getDuration());
		result.setDistance((double) totalDistance);
		result.setGroup(group);
		new GameDao().saveOrUpdate(result);
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
	
	@FXML
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
		obstacleEdge.setOpacity(0.30);
		
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
				gameResourceManager.getPlayerImage(playerTileImage.getImage(), playerTileImage.getFileType()) : null;
		
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

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	
}