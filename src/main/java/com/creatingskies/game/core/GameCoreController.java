package com.creatingskies.game.core;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

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

import javafx.animation.Animation.Status;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonType;
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
import javafx.scene.shape.StrokeType;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.util.Duration;

public class GameCoreController extends PropertiesViewController {
	
	private static final Double DEFAULT_SCALE_FACTOR = 15.0;
	private static final Double PREFERRED_MINIMAP_TILE_COUNT_X = 25.0;
	private static final Double PREFERRED_MINIMAP_TILE_COUNT_Y = 25.0;
	private static final Double MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER = 1.25;
	private static final double INITIAL_PLAYER_ROTATION = -90.0;
	
	@FXML private Pane pane;
	@FXML private AnchorPane mainContainer;
	@FXML private AnchorPane weatherContainer;
	@FXML private GridPane mapTiles;
	@FXML private Pane miniMapPane;
	
	@FXML private Label countDownValue;
	@FXML private Label durationLabel;
	@FXML private Label distanceLabel;
	@FXML private Label speedLabel;
	
	@FXML private Label obstacleSlowLabel;
	@FXML private Label tileSlowLabel;
	@FXML private Label weatherSlowLabel;
	
	@FXML private ImageView warningImageView;
	@FXML private ImageView stopImageView;
	
	@FXML private ImageView tiltUpImageView;
	@FXML private ImageView tiltDownImageView;
	@FXML private ImageView tiltLeftImageView;
	@FXML private ImageView tiltRightImageView;
	
	@FXML private Label difficultyLevel;
	@FXML private Label verticalTiltLevel;
	@FXML private Label horizontalTiltLevel;
	
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
	
	private Tile startTilePoint;
	
	private Double scaleFactorX = 15.0;
	private Double scaleFactorY = 15.0;
	
	private Integer verticalTilt = 0;
	private Integer horizontalTilt = 0;
	
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
		inputReader = new K8055AnalogInputReader();
		
		super.init();
		initDashboard();
		
		MapDao mapDao = new MapDao();
		Map map = mapDao.findMapWithDetails(getGameEvent().getGame().getMap().getIdNo());
		
		gameResourceManager = new GameResourcesManager(((GameEvent) getCurrentRecord()).getGame(),weatherContainer);
		
		initScaleFactors(map.getWidth(), map.getHeight());
		initPlayer(getGameEvent().getGame().getType());
		initGameLoop();
		initCountdownTimer();
		
		initMap(map);
		initWarningImages();
		initWeathers();
		
		inputReader.init();
		countDownTimer.play();
	}
	
	private void initDashboard(){
		tiltUpImageView.setVisible(verticalTilt > 0);
		tiltDownImageView.setVisible(verticalTilt < 0);
		tiltRightImageView.setVisible(horizontalTilt > 0);
		tiltLeftImageView.setVisible(horizontalTilt < 0);
		
		verticalTiltLevel.setText(String.valueOf(Math.abs(verticalTilt)));
		horizontalTiltLevel.setText(String.valueOf(Math.abs(horizontalTilt)));
		difficultyLevel.setText(String.valueOf(getTotalSlowFactor()));
	}
	
	private void resetsPlayerPosition(){
		player.setLayoutX(startTilePoint.getColIndex() * getMainScreenTileWidth());
		player.setLayoutY(startTilePoint.getRowIndex() * getMainScreenTileHeight());
		
		miniPlayer.setLayoutX(startTilePoint.getColIndex() * getMiniScreenTileWidth());
		miniPlayer.setLayoutY(startTilePoint.getRowIndex() * getMiniScreenTileHeight());
		
		scrollMap();
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
				
				frontImage.setFitWidth(getMainScreenTileWidth());
				frontImage.setFitHeight(getMainScreenTileHeight());
				
				if(tile.getObstacle() != null){
					frontImage.setImage(gameResourceManager.getObstacleImage(tile.getObstacle()));
				} else {
					frontImage.setImage(Util.byteArrayToImage(tile.getFrontImage().getImage()));
				}
				
				miniFrontImage.setImage(Util.byteArrayToImage(tile.getObstacle() != null ?
						tile.getObstacle().getImage() : tile.getFrontImage().getImage()));
				
				if(tile.getStartPoint() || tile.getEndPoint()){
					miniFrontImage.setFitWidth(getMiniScreenTileWidth() * MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER);
					miniFrontImage.setFitHeight(getMiniScreenTileHeight() * MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER);
				} else {
					miniFrontImage.setFitWidth(getMiniScreenTileWidth());
					miniFrontImage.setFitHeight(getMiniScreenTileHeight());
				}
				
				if(tile.getObstacle() != null){
					createObstacle(tile);
				} else if(tile.getStartPoint()){
					startTilePoint = tile;
					player.setLayoutX(tile.getColIndex() * getMainScreenTileWidth());
					player.setLayoutY(tile.getRowIndex() * getMainScreenTileHeight());
					pane.getChildren().add(player);
					
					miniPlayer.setLayoutX(tile.getColIndex() * getMiniScreenTileWidth());
					miniPlayer.setLayoutY(tile.getRowIndex() * getMiniScreenTileHeight());
					miniMapPane.getChildren().add(miniPlayer);
				} else if(tile.getEndPoint()){
					createEndRectangle(tile);
				}
			}
			
			if (tile.getObstacle() != null
					|| tile.getStartPoint()
					|| tile.getEndPoint()
					|| (tile.getBackImage() != null
						&& ((tile.getBackImage().getVerticalTilt() != null && tile.getBackImage().getVerticalTilt() != 0) 
							|| (tile.getBackImage().getHorizontalTilt() != null && tile.getBackImage().getHorizontalTilt() != 0)))) {
				createTileShapes(tile, map.getDefaultTileImage());
			}
			
			mapTiles.add(Util.bundle(backImage, frontImage), tile.getColIndex(), tile.getRowIndex());
			miniMapPane.getChildren().add(Util.bundle(tile.getColIndex() * getMiniScreenTileWidth(),
					tile.getRowIndex() * getMiniScreenTileHeight(), miniBackImage, miniFrontImage));
		}
		
		miniPlayer.toFront();
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
		degreesPreferred = INITIAL_PLAYER_ROTATION;
		player.setRotate(degreesPreferred);
		miniPlayer.setRotate(degreesPreferred);
		gameLoop.play();
		gameResourceManager.start();
	}
	
	private void initGameLoop() {
		gameLoop = new Timeline(new KeyFrame(Duration.millis(Constant.FRAME_DURATION),
				new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	if(gameLoop.getStatus() != Status.PAUSED){
		    		millisGameDuration += Constant.FRAME_DURATION;
			    	durationLabel.setText(String.format("%.2f", getDuration()));
			    	distanceLabel.setText(String.format("%.2f", totalDistance));
			    	speedLabel.setText(String.format("%.2f", Util.computeSpeed(distance)) + " m/s");
			    	
			    	inputForce = inputReader.readInput();
			    	
			    	if(inputReader.isResetButtonPressed()){
			    		inputReader.setResetButtonPressed(false);
			    		resetsPlayerPosition();
			    	}
			    	
			    	if(inputReader.isQuitButtonPressed()){
			    		gameLoop.pause();
			    		confirmQuit();
			    	}
			    	
			    	if(inputForce.left != 0 || inputForce.right != 0){
			    		inputForce.left = inputForce.left == 0 ? 1 : inputForce.left;
						inputForce.right = inputForce.right == 0 ? 1 : inputForce.right;
			    	}
			    	
			    	computeRotation();
					computeMovement();
					initDashboard();
		    	}
		    }
		}));
		gameLoop.setCycleCount(Timeline.INDEFINITE);
	}
	
	private void confirmQuit() {
		Platform.runLater(new Runnable(){
			@Override
			public void run() {
				Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Confirmation Dialog",
						"Are you sure you want to quit this game?", null).showAndWait();
				
				if(result.get() == ButtonType.OK){
					closeGame();
					return;
				} else {
					inputReader.setQuitButtonPressed(false);
				}
				gameLoop.play();
			}
		});
		
	}
	
	private void computeRotation(){
		Double currentDeg = player.getRotate();
		Double deltaDeg = ((45 / Constant.MAX_MOVESPEED) * (inputForce.left - inputForce.right));
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

		totalSlowFactor = getTotalSlowFactor();
		
		if(inputForce.left != 0 && inputForce.right != 0){
			distance = Math.max((((inputForce.left + inputForce.right)
					/ (Constant.MAX_MOVESPEED * 2)) * Constant.MAX_MOVESPEED) - totalSlowFactor, 0.1);
			
			totalDistance += distance;
			
			double cosValue = (distance * Math.cos(Math.toRadians(player.getRotate())));
			double sinValue = (distance * Math.sin(Math.toRadians(player.getRotate())));
			
			player.setLayoutX(player.getLayoutX() + cosValue);
			player.setLayoutY(player.getLayoutY() + sinValue);
			miniPlayer.setLayoutX(miniPlayer.getLayoutX() + (cosValue / scaleFactorX));
			miniPlayer.setLayoutY(miniPlayer.getLayoutY() + (sinValue / scaleFactorY));
			
			scrollMap();
			
			if(checkCollision(playerCircle)){
				encounteredBlockage = true;
				player.setLayoutX(player.getLayoutX() - cosValue);
				player.setLayoutY(player.getLayoutY() - sinValue);
				miniPlayer.setLayoutX(miniPlayer.getLayoutX() - (cosValue / scaleFactorX));
				miniPlayer.setLayoutY(miniPlayer.getLayoutY() - (sinValue / scaleFactorY));
			}
		}
		
		inputReader.display(distance, totalSlowFactor, player.getRotate(), verticalTilt, horizontalTilt);
		warningImageView.setVisible(warningImageView.isVisible() && !encounteredBlockage);
		stopImageView.setVisible(encounteredBlockage);
		
	}
	
	private void scrollMap(){
		if((player.getLayoutX() + ((stage.getWidth() + player.getWidth()) / 2)) < pane.getBoundsInLocal().getWidth()){
			if((0-(player.getLayoutX() - ((stage.getWidth() - player.getWidth()) / 2))) < 0){
				pane.setLayoutX(0-(player.getLayoutX() - ((stage.getWidth() - player.getWidth()) / 2)));
			} else {
				pane.setLayoutX(0.0);
			}
		}
		
		if((player.getLayoutY() + ((stage.getHeight() + player.getHeight()) / 2)) < pane.getBoundsInLocal().getHeight()){
			if((0-(player.getLayoutY() - ((stage.getHeight() - player.getHeight()) / 2))) < 0){
				pane.setLayoutY(0-(player.getLayoutY() - ((stage.getHeight() - player.getHeight()) / 2)));
			} else {
				pane.setLayoutY(0.0);
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
				TileValueHolder holder = (TileValueHolder) tileShape.getUserData();
				
				if(holder != null && (holder.verticalTilt != 0 || holder.horizontalTilt != 0)){
					tileSlowFactor = (double) holder.difficulty;
					verticalTilt = holder.verticalTilt;
					horizontalTilt = holder.horizontalTilt;
					
					tileSlowLabel.setText(String.format("%.2f", tileSlowFactor));
					return;
				}
			}
		}
		
		tileSlowFactor = 0.0;
		verticalTilt = 0;
		horizontalTilt = 0;
		tileSlowLabel.setText(String.format("%.2f", tileSlowFactor));
	}
	
	private void checkGameStatus(Shape block){
		Shape intersect = Shape.intersect(block, endTile);
		if (intersect.getBoundsInLocal().getWidth() != -1) {
			gameLoop.stop();
			gameResourceManager.stop();
			
			saveGameResult();
			
			Platform.runLater(new Runnable() {
				
				@Override
				public void run() {
					new AlertDialog(AlertType.INFORMATION, "Finish!",  "Congratulations!",
							" Company: " + group.getCompany().getName()
							+ "\n Group: " + group.getName()
							+ "\n Duration: " + String.format("%.2f", getDuration())
							+ "\n Average Speed: " + String.format("%.2f", Util.computeSpeed(totalDistance, getDuration())))
							.showAndWait();

					close();
					stage.close();
				}
			});
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
				o.getStrokeDashArray().clear();
				o.getStrokeDashArray().addAll(10.0, 20.0);
				o.setStroke(Color.DARKRED);
			} else {
				o.setStroke(null);
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
		setCurrentRecord(null);
		inputReader.destroy();
		gameLoop = null;
		mapTiles.getChildren().clear();
		obstacles.clear();
		obstacleEdges.clear();
		gameResourceManager.stop();
		super.close();
	}
	
	@Override
	protected void finalize() throws Throwable {
		super.finalize();
		close();
	}
	
	public void createTileShapes(Tile tile, TileImage defaultTileImage){
		Rectangle tileShape = createDefaultRectangle(tile);
		
		TileValueHolder holder = new TileValueHolder();
		holder.difficulty = tile.getBackImage() != null ?
				tile.getBackImage().getDifficulty() : defaultTileImage.getDifficulty();
		holder.verticalTilt = tile.getBackImage() != null ?
				tile.getBackImage().getVerticalTilt() : defaultTileImage.getVerticalTilt();
		holder.horizontalTilt = tile.getBackImage() != null ?
				tile.getBackImage().getHorizontalTilt() : defaultTileImage.getHorizontalTilt();
		
		tileShape.setUserData(holder);
		pane.getChildren().add(tileShape);
		tileShapes.add(tileShape);
	}
	
	public void createObstacle(Tile tile){
		Rectangle obstacle = createDefaultRectangle(tile);
		obstacle.setStrokeWidth(2.0);
		obstacle.setStrokeType(StrokeType.INSIDE);
		pane.getChildren().add(obstacle);
		obstacles.add(obstacle);
		createObstacleEdge(tile);
	}
	
	private void createObstacleEdge(Tile tile){
		Circle obstacleEdge = new Circle();
		obstacleEdge.setRadius(getMainScreenTileWidth() * tile.getObstacleRadius());
		obstacleEdge.setUserData(tile.getObstacleDifficulty());
		
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
		
		miniPlayerCircle = new Circle((getMiniScreenTileWidth() * MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER) / 2, Color.BLACK);
		miniPlayerCircle.setOpacity(0.2);
		
		ImageView miniplayerImageView = new ImageView();
		miniplayerImageView.setFitWidth(getMiniScreenTileWidth() * MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER);
		miniplayerImageView.setFitHeight(getMiniScreenTileHeight() * MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER);
		miniplayerImageView.setImage(playerImage);
		
		miniPlayer = new StackPane();
		miniPlayer.setPrefWidth(getMiniScreenTileWidth() * MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER);
		miniPlayer.setPrefHeight(getMiniScreenTileHeight() * MINIMAP_CORE_IMAGE_SIZE_MULTIPLIER);
		miniPlayer.getChildren().addAll(miniPlayerCircle, miniplayerImageView);
		
		player.setRotate(INITIAL_PLAYER_ROTATION);
		miniPlayer.setRotate(INITIAL_PLAYER_ROTATION);
	}
	
	private double getMainScreenTileWidth(){
		return 240.0;
	}
	
	private double getMainScreenTileHeight(){
		return 240.0;
	}
	
	private double getMiniScreenTileWidth(){
		return getMainScreenTileWidth() / scaleFactorX;
	}
	
	private double getMiniScreenTileHeight(){
		return getMainScreenTileHeight() / scaleFactorY;
	}
	
	private void initScaleFactors(Integer mapTileCountX, Integer mapTileCountY){
		scaleFactorX = DEFAULT_SCALE_FACTOR * (mapTileCountX / PREFERRED_MINIMAP_TILE_COUNT_X);
		scaleFactorY = DEFAULT_SCALE_FACTOR * (mapTileCountY / PREFERRED_MINIMAP_TILE_COUNT_Y);
	}

	public void setStage(Stage stage) {
		this.stage = stage;
	}

	public void setGroup(Group group) {
		this.group = group;
	}
	
	private Double getTotalSlowFactor(){
		Double slowSummation = weatherSlowFactor + obstacleSlowFactor + tileSlowFactor;
		return Math.min(slowSummation, Constant.MAX_MOVESPEED);
	}
	
	protected class TileValueHolder {
		public Integer difficulty = 0;
		public Integer verticalTilt = 0;
		public Integer horizontalTilt = 0;
	}
	
}
