package com.creatingskies.game.core;

import java.util.concurrent.ConcurrentHashMap;

import com.creatingskies.game.classes.AbstractInputReader;
import com.creatingskies.game.common.MainLayout;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import k8055.K8055JavaCall;

public class KeyboardInputReader extends AbstractInputReader {

	private InputForce inputForce;
	private ConcurrentHashMap<KeyCode, Integer> leftCodes;
	private ConcurrentHashMap<KeyCode, Integer> rightCodes;
	
	private K8055JavaCall k8055;
	
	private long interval = 50; 
	private long duration = 0;
	
	private int previousVerticalTiltValue = 0;
	private int previousHorizontalTiltValue = 0;
	
	private Integer verticalChannels[] = {1, 2, 3, 4};
	private Integer horizontalChannels[] = {5, 6, 7, 8};
	
	@Override
	public void init(){
		inputForce = new InputForce();
		
		k8055 = new K8055JavaCall();
		k8055.OpenDevice(0);
		
		initKeyCodes();
		initKeyboardListeners();
	}
	
	@Override
	public InputForce readInput() {
		return inputForce;
	}
	
	@Override
	public void destroy() {
		k8055.ClearAllDigital();
		k8055.CloseDevice();
	}
	
	public void initKeyCodes() {
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
	
	public void initKeyboardListeners() {
		MainLayout.getModalLayout().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(leftCodes.containsKey(event.getCode())){
					inputForce.left = leftCodes.get(event.getCode());
				}
				
				if(rightCodes.containsKey(event.getCode())){
					inputForce.right = rightCodes.get(event.getCode());
				}
				
				if(event.getCode() == KeyCode.F1){
					setResetButtonPressed(true);
				}
				
				if(event.getCode() == KeyCode.F2){
					setQuitButtonPressed(true);
				}
			}
		});
		
		MainLayout.getModalLayout().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				inputForce.left = 0;
				inputForce.right = 0;
				setQuitButtonPressed(false);
				setResetButtonPressed(false);
			}
		});
	}
	
	@Override
	public void display(Double speed, Double slowFactor, Double degree,
			Integer verticalTilt, Integer horizontalTilt) {
		duration += interval;
		if(duration >= 500){
			duration = 0;
			
			boolean shouldUpdateTiltDisplay = false;
			
			if(previousVerticalTiltValue < verticalTilt){
				shouldUpdateTiltDisplay = true;
				previousVerticalTiltValue++;
			} else if(previousVerticalTiltValue > verticalTilt){
				shouldUpdateTiltDisplay = true;
				previousVerticalTiltValue--;
			}
			
			if(previousHorizontalTiltValue < horizontalTilt){
				shouldUpdateTiltDisplay = true;
				previousHorizontalTiltValue++;
			} else if(previousHorizontalTiltValue > horizontalTilt){
				shouldUpdateTiltDisplay = true;
				previousHorizontalTiltValue--;
			}
			
			if(shouldUpdateTiltDisplay){
				k8055.ClearAllDigital();
				displayTilt(previousVerticalTiltValue, true);
				displayTilt(previousHorizontalTiltValue, false);
			}
		}
	}
	
	private void displayTilt(int data, boolean forVertical){
		Integer[] channels = forVertical ? verticalChannels : horizontalChannels;
		
		if(data > 0){
			k8055.SetDigitalChannel(channels[0]);
		}
		
		data = Math.abs(data);
		
		if(data >= 4){
			k8055.SetDigitalChannel(channels[3]);
			data -= 4;
		}
		
		if(data >= 2){
			k8055.SetDigitalChannel(channels[2]);
			data -= 2;
		}
		
		if(data >= 1){
			k8055.SetDigitalChannel(channels[1]);
			data -= 1;
		}
	}

}
