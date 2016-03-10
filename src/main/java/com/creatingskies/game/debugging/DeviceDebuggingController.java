package com.creatingskies.game.debugging;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.creatingskies.game.classes.ViewController;
import com.creatingskies.game.common.MainLayout;
import com.creatingskies.game.core.K8055AnalogInputReader;

import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.Slider;
import javafx.scene.layout.AnchorPane;
import javafx.scene.paint.Color;
import javafx.scene.paint.Paint;
import javafx.scene.shape.Circle;

public class DeviceDebuggingController extends ViewController {
	
	/**
	 * TODO Re-factor. Used implementation-specific class to call
	 * directly displayTilt on reader, can't use display method
	 * on parent due to delay from previous display method call computation
	 * and value change step by step display moderation.
	 */
	private K8055AnalogInputReader reader = new K8055AnalogInputReader();

	@FXML Slider difficultySlider;
	@FXML Slider verticalSlider;
	@FXML Slider horizontalSlider;
	
	@FXML Circle c1;
	@FXML Circle c2;
	@FXML Circle c3;
	@FXML Circle c4;
	@FXML Circle c5;
	@FXML Circle c6;
	@FXML Circle c7;
	@FXML Circle c8;
	
	private List<Circle> difficultyCircles;
	private List<Circle> verticalCircles;
	private List<Circle> horizontalCircles;
	
	private Paint defaultFill;
	
	public void initialize(){
		super.init();
		difficultyCircles = new ArrayList<Circle>();
		verticalCircles = new ArrayList<Circle>();
		horizontalCircles = new ArrayList<Circle>();
		reader.init();
	}
	
	@Override
	protected String getViewTitle() {
		return "Debugging";
	}
	
	public void show(){
		try {
            FXMLLoader loader = new FXMLLoader();
            loader.setLocation(getClass().getResource("Debugging.fxml"));
            AnchorPane login = (AnchorPane) loader.load();
            MainLayout.getRootLayout().setCenter(login);
        } catch (IOException e) {
            e.printStackTrace();
        }
	}
	
	@FXML
	public void testDisplayTiltValues(){
		if(defaultFill == null){
			defaultFill = c1.getFill();
			difficultyCircles.addAll(Arrays.asList(c1, c2));
			verticalCircles.addAll(Arrays.asList(c3, c4, c5));
			horizontalCircles.addAll(Arrays.asList(c6, c7, c8));
		}
		
		clearCirclesFill();
		displayDifficulty((int) difficultySlider.getValue());
		displayTilt((int) verticalSlider.getValue(), true);
		displayTilt((int) horizontalSlider.getValue(), false);
		
		reader.clearAllDigital();
		reader.displayDifficulty((int) difficultySlider.getValue());
		reader.displayTilt((int) verticalSlider.getValue(), true);
		reader.displayTilt((int) horizontalSlider.getValue(), false);
	}
	
	private void clearCirclesFill(){
		for(Circle c : difficultyCircles){
			c.setFill(Color.WHITESMOKE);
		}
		
		for(Circle c : horizontalCircles){
			c.setFill(Color.WHITESMOKE);
		}
		
		for(Circle c : verticalCircles){
			c.setFill(Color.WHITESMOKE);
		}
	}
	
	private void displayTilt(int data, boolean forVertical){
		List<Circle> circles = forVertical ? verticalCircles : horizontalCircles;

		if(data > 0){
			circles.get(0).setFill(defaultFill);
		}
		
		data = Math.abs(data);
		
		if(data >= 2){
			circles.get(2).setFill(defaultFill);
			data -= 2;
		}
		
		if(data >= 1){
			circles.get(1).setFill(defaultFill);
			data -= 1;
		}
	}
	
	public void displayDifficulty(int data){
		if(data >= 2){
			c2.setFill(defaultFill);
			data -= 2;
		}
		
		if(data >= 1){
			c1.setFill(defaultFill);
			data -= 1;
		}
	}
	
	@Override
	protected void close() {
		super.close();
		reader.destroy();
	}

}
