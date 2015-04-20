package com.creatingskies.game.classes;

public abstract class AbstractInputReader {
	
	public abstract void init();
	public abstract void destroy();
	public abstract InputForce readInput();
	public abstract void display(Double speed, Double slowFactor, Double degree);
	
	public class InputForce {
		public Integer left = 0;
		public Integer right = 0;
	}
	
}

