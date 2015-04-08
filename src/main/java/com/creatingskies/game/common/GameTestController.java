package com.creatingskies.game.common;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Pane;
import javafx.scene.layout.StackPane;
import javafx.scene.shape.Rectangle;
import javafx.scene.shape.Shape;
import javafx.util.Duration;
import k8055.K8055JavaCall;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.main.MainController;

public class GameTestController extends ViewController {

	private boolean playFromDevice;
	
	private Map<KeyCode, Integer> leftCodes;
	private Map<KeyCode, Integer> rightCodes;
	private List<Shape> obstacles;
	
	private Double speedFactor = 0.5, weatherSlowFactor = 0.0, obstacleSlowFactor = 0.0;
	private Double maxPow = 3.0, minPow = 1.0;
	private Integer leftPow = 0, rightPow = 0;
	
	private Double preferredDeg = 0.0;
	private Delta initialDelta;
	private Timeline timeline;
	
	@FXML Rectangle rectangleObstacle1;
	@FXML Rectangle rectangleObstacle2;
	@FXML Rectangle safeHaven;
	
	@FXML StackPane playerStackPane;
	@FXML ImageView playerImageView;
	@FXML Rectangle playerRectangle;
	private Node playerNode;
	
	private long gameStartMillis;
	
	private K8055JavaCall k8055 = new K8055JavaCall();

	@Override
	protected String getViewTitle() {
		return "Game Test";
	}

	public void show() {
		try {
			FXMLLoader loader = new FXMLLoader();
			loader.setLocation(getClass().getResource("GameTest.fxml"));
			Pane pane = (Pane) loader.load();
			MainLayout.getRootLayout().setCenter(pane);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@FXML
	public void initialize() {
		super.init();
		obstacles = new ArrayList<Shape>();
		obstacles.add(rectangleObstacle1);
		obstacles.add(rectangleObstacle2);
		
		initImageView();
		playerNode = playerStackPane;

		playerStackPane.getChildren().addAll(playerRectangle, playerImageView);
		
		initialDelta = new Delta();
		initialDelta.x = playerNode.getLayoutX();
		initialDelta.y = playerNode.getLayoutY();
		
		AlertDialog dialog = new AlertDialog(AlertType.INFORMATION,
				"Select Mode", "Choose input.", null);
		
		ButtonType deviceOption = new ButtonType("DEVICE");
		ButtonType keyboardOption = new ButtonType("KEYBOARD");

		dialog.getButtonTypes().clear();
		dialog.getButtonTypes().addAll(deviceOption, keyboardOption);
		
		Optional<ButtonType> result = dialog.showAndWait();
		
		if(result.get() == deviceOption){
			playFromDevice = true;
			initDevice();
		} else {
			playFromDevice = false;
			initKeyCodes();
			initKeyboardListeners();
		}
		
		initTimeline();
		handleReset();
	}
	
	@FXML
	private void handleReset(){
		leftPow = 0;
		rightPow = 0;
		preferredDeg = 0.0;
		playerNode.setRotate(preferredDeg);
		
		gameStartMillis = System.currentTimeMillis();
		
		playerNode.setLayoutX(initialDelta.x);
		playerNode.setLayoutY(initialDelta.y);
		
		timeline.play();
	}

	private void initKeyboardListeners() {
		MainLayout.getRootLayout().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(leftCodes.containsKey(event.getCode())){
					leftPow = leftCodes.get(event.getCode());
				}
				
				if(rightCodes.containsKey(event.getCode())){
					rightPow = rightCodes.get(event.getCode());
				}
			}
		});
		
		MainLayout.getRootLayout().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				leftPow = 0;
				rightPow = 0;
			}
		});
	}

	public void initDevice() {
		k8055.OpenDevice(0);
	}

	private void initImageView() {
		Image image = new Image("/images/cyclist.png");
		playerImageView.setImage(image);
	}

	private void initTimeline() {
		timeline = new Timeline(new KeyFrame(Duration.millis(50),
				new EventHandler<ActionEvent>() {
		    @Override
		    public void handle(ActionEvent event) {
		    	if(playFromDevice){
		    		readFromDevice();
		    	}
		    	
				if(rightPow == 0 || leftPow == 0){
					
				} else {
					Double speed = (leftPow + rightPow) * (speedFactor + weatherSlowFactor + obstacleSlowFactor);
					
					Double currentDeg = playerNode.getRotate();
					Double deltaDeg = ((45/(maxPow - minPow)) * (leftPow - rightPow));
					
					preferredDeg = currentDeg + deltaDeg;
					
					if(currentDeg.compareTo(preferredDeg) != 0){
						currentDeg += currentDeg.compareTo(preferredDeg) < 0 ? 1 : -1;
					}
					
					playerNode.setRotate(currentDeg);
					playerNode.setLayoutX(playerNode.getLayoutX() + (speed * Math.cos(Math.toRadians(currentDeg))));
					playerNode.setLayoutY(playerNode.getLayoutY() + (speed * Math.sin(Math.toRadians(currentDeg))));
					
					System.out.println("X: " + (speed * Math.cos(Math.toRadians(currentDeg))));
					System.out.println("Y: " + (speed * Math.sin(Math.toRadians(currentDeg))));
					
					checkCollision(playerRectangle);
				}
		    }
		}));
		timeline.setCycleCount(Timeline.INDEFINITE);
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
	
	private void initKeyCodes() {
		leftCodes = new ConcurrentHashMap<KeyCode, Integer>();
		leftCodes.put(KeyCode.DIGIT1, 1);
		leftCodes.put(KeyCode.DIGIT2, 2);
		leftCodes.put(KeyCode.DIGIT3, 3);
		leftCodes.put(KeyCode.DIGIT4, 4);
		leftCodes.put(KeyCode.DIGIT5, 5);
		leftCodes.put(KeyCode.DIGIT6, 6);
		leftCodes.put(KeyCode.DIGIT7, 7);
		leftCodes.put(KeyCode.DIGIT8, 8);
		leftCodes.put(KeyCode.DIGIT9, 9);
		
		rightCodes = new ConcurrentHashMap<KeyCode, Integer>();
		rightCodes.put(KeyCode.NUMPAD1, 1);
		rightCodes.put(KeyCode.NUMPAD2, 2);
		rightCodes.put(KeyCode.NUMPAD3, 3);
		rightCodes.put(KeyCode.NUMPAD4, 4);
		rightCodes.put(KeyCode.NUMPAD5, 5);
		rightCodes.put(KeyCode.NUMPAD6, 6);
		rightCodes.put(KeyCode.NUMPAD7, 7);
		rightCodes.put(KeyCode.NUMPAD8, 8);
		rightCodes.put(KeyCode.NUMPAD9, 9);
	}
	
	private void checkCollision(Shape block) {
		for (Shape o : obstacles) {
			if (o != block) {
				Shape intersect = Shape.intersect(block, o);
				if (intersect.getBoundsInLocal().getWidth() != -1) {
					timeline.stop();
					
					Optional<ButtonType> result = new AlertDialog(AlertType.CONFIRMATION, "Deads", "reported!", "Retry?").showAndWait();
					if(result.get() == ButtonType.OK){
						handleReset();
					} else {
						new AlertDialog(AlertType.ERROR, "ZZ", "reported!", "Quitters amp").show();
						new MainController().show();
					}
					break;
				}
			}
		}
		
		Shape finishLineIntersection = Shape.intersect(block, safeHaven);
		if (finishLineIntersection.getBoundsInLocal().getWidth() != -1) {
			timeline.stop();
			long duration = TimeUnit.MILLISECONDS.toSeconds((System.currentTimeMillis() - gameStartMillis));
			new AlertDialog(AlertType.INFORMATION, "Edi wow", "ez monitors", "Duration: " + duration).showAndWait();
		}
	}

	class Delta {
		double x, y;
	}
	
}
