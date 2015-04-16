package com.creatingskies.game.core;

import k8055.K8055JavaCall;

import com.creatingskies.game.classes.AbstractInputReader;

public class K8055InputReader extends AbstractInputReader {

	private InputForce inputForce;
	private K8055JavaCall k8055;
	
	@Override
	public void init() {
		k8055 = new K8055JavaCall();
		k8055.OpenDevice(0);
	}

	@Override
	public void destroy() {
		k8055.CloseDevice();
	}

	@Override
	public InputForce readInput() {
		inputForce.left = 0;
		inputForce.right = 0;
		
		try {
        	if(k8055.ReadDigitalChannel(1) == 1) inputForce.left += 1;
        	if(k8055.ReadDigitalChannel(2) == 1) inputForce.left += 2;
        	
        	if(k8055.ReadDigitalChannel(4) == 1) inputForce.right += 1;
        	if(k8055.ReadDigitalChannel(5) == 1) inputForce.right += 2;
        	
        } catch (Exception e){
        	e.printStackTrace();
        	k8055.CloseDevice();
        }
		
		return inputForce;
	}

}
