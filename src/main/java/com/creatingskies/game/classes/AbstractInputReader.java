package com.creatingskies.game.classes;

public abstract class AbstractInputReader {
	
	public abstract void init();
	public abstract void destroy();
	public abstract InputForce readInput();
	public abstract void display(Double speed, Double slowFactor, Double degree,
			Integer verticalTilt, Integer horizontalTilt);
	
	private boolean isResetButtonPressed = false;
	private boolean isQuitButtonPressed = false;
	
	public class InputForce {
		public Integer left = 0;
		public Integer right = 0;
	}
	
	public boolean isResetButtonPressed() {
		return isResetButtonPressed;
	}
	
	public void setResetButtonPressed(boolean isResetButtonPressed) {
		this.isResetButtonPressed = isResetButtonPressed;
	}
	
	public boolean isQuitButtonPressed() {
		return isQuitButtonPressed;
	}
	
	public void setQuitButtonPressed(boolean isQuitButtonPressed) {
		this.isQuitButtonPressed = isQuitButtonPressed;
	}
}

