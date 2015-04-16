package com.creatingskies.game.core;

import java.util.concurrent.ConcurrentHashMap;

import javafx.event.EventHandler;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

import com.creatingskies.game.classes.AbstractInputReader;
import com.creatingskies.game.common.MainLayout;

public class KeyboardInputReader extends AbstractInputReader {

	private InputForce inputForce;
	private ConcurrentHashMap<KeyCode, Integer> leftCodes;
	private ConcurrentHashMap<KeyCode, Integer> rightCodes;
	
	@Override
	public void init(){
		inputForce = new InputForce();
		initKeyCodes();
		initKeyboardListeners();
	}
	
	@Override
	public InputForce readInput() {
		return inputForce;
	}
	
	@Override
	public void destroy() {}
	
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
		MainLayout.getRootLayout().setOnKeyPressed(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				if(leftCodes.containsKey(event.getCode())){
					inputForce.left = leftCodes.get(event.getCode());
				}
				
				if(rightCodes.containsKey(event.getCode())){
					inputForce.right = rightCodes.get(event.getCode());
				}
			}
		});
		
		MainLayout.getRootLayout().setOnKeyReleased(new EventHandler<KeyEvent>() {
			@Override
			public void handle(KeyEvent event) {
				inputForce.left = 0;
				inputForce.right = 0;
			}
		});
	}

}
