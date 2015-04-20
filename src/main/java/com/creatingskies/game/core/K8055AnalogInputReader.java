package com.creatingskies.game.core;

import k8055.K8055JavaCall;

import com.creatingskies.game.classes.AbstractInputReader;

public class K8055AnalogInputReader extends AbstractInputReader {

	private InputForce inputForce;
	private K8055JavaCall k8055;
	
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
		inputForce.right = (int) (k8055.ReadAnalogChannel(1) * 7) / 255;
		inputForce.left = (int) (k8055.ReadAnalogChannel(2) * 7) / 255;
		return inputForce;
	}

	@Override
	public void display(Double speed, Double slowFactor, Double degree) {
		display(slowFactor.intValue());
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
