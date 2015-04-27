package com.creatingskies.game.core;

import k8055.K8055JavaCall;

import com.creatingskies.game.classes.AbstractInputReader;

public class K8055AnalogInputReader extends AbstractInputReader {

	private InputForce inputForce;
	private K8055JavaCall k8055;
	
	private long interval = 50; 
	private long duration = 0;
	private int previousValue = 0;
	
	@Override
	public void init() {
		inputForce = new InputForce();
		k8055 = new K8055JavaCall();
		k8055.OpenDevice(0);
	}

	@Override
	public void destroy() {
		k8055.CloseDevice();
	}

	@Override
	public InputForce readInput() {
		inputForce.right = (int) ((k8055.ReadAnalogChannel(1) * 7) / 255) * 2;
		inputForce.left = (int) ((k8055.ReadAnalogChannel(2) * 7) / 255) * 2;
		return inputForce;
	}

	@Override
	public void display(Double speed, Double slowFactor, Double degree) {
		duration += interval;
		if(duration >= 500){
			duration = 0;
			
			if(previousValue < slowFactor.intValue()){
				display(++previousValue);
			} else if(previousValue > slowFactor.intValue()){
				display(--previousValue);
			}
		}
	}
	
	private void display(int data){
		k8055.ClearAllDigital();
		
		if(data >= 4){
			k8055.SetDigitalChannel(3);
			data -= 4;
		}
		
		if(data >= 2){
			k8055.SetDigitalChannel(2);
			data -= 2;
		}
		
		if(data >= 1){
			k8055.SetDigitalChannel(1);
			data -= 1;
		}
	}

}
