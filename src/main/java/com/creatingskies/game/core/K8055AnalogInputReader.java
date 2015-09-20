package com.creatingskies.game.core;

import k8055.K8055JavaCall;

import com.creatingskies.game.classes.AbstractInputReader;

public class K8055AnalogInputReader extends AbstractInputReader {

	private InputForce inputForce;
	private K8055JavaCall k8055;
	
	private long interval = 50; 
	private long duration = 0;
	
	private int previousVerticalTiltValue = 0;
	private int previousHorizontalTiltValue = 0;
	
	private Integer[] verticalChannels;
	private Integer[] horizontalChannels;
	
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
		
		if(k8055.ReadDigitalChannel(1) == 1) {
			setResetButtonPressed(true);
		}else{
			setResetButtonPressed(false);
		}
    	if(k8055.ReadDigitalChannel(2) == 1) {
    		setQuitButtonPressed(true);
    	}else{
    		setQuitButtonPressed(false);
    	}
		
		return inputForce;
	}

	@Override
	public void display(Double speed, Double slowFactor, Double degree,
			Integer verticalTilt, Integer horizontalTilt) {
		duration += interval;
		if(duration >= 500){
			duration = 0;
			
			if(previousVerticalTiltValue < verticalTilt){
				displayTilt(++previousVerticalTiltValue, true);
			} else if(previousVerticalTiltValue > verticalTilt){
				displayTilt(--previousVerticalTiltValue, true);
			}
			
			if(previousHorizontalTiltValue < horizontalTilt){
				displayTilt(++previousHorizontalTiltValue, false);
			} else if(previousHorizontalTiltValue > horizontalTilt){
				displayTilt(--previousHorizontalTiltValue, false);
			}
		}
	}
	
	
	private void displayTilt(int data, boolean forVertical){
		Integer[] channels = forVertical ? verticalChannels : horizontalChannels;
		
		for(Integer channel : channels){
			k8055.ClearDigitalChannel(channel);
		}

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
