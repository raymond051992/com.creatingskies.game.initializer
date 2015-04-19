package com.creatingskies.game.core.resources;

import java.io.File;
import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import org.apache.commons.io.FileUtils;

import com.creatingskies.game.core.Game;

public class GameResourcesManager {
	
	private String sessionId;
	private Game game;
	private CyclicBarrier cyclicBarrier;
	
	private Thread audioThread;
	private GameAudioResource gameAudioResource;
	
	public static void removeTmpFiles(){
		try {
			FileUtils.deleteDirectory(new File("game/tmp/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public GameResourcesManager(final Game game) {
		cyclicBarrier = new CyclicBarrier(2);
		sessionId = UUID.randomUUID().toString();
		this.game = game;
		
		initResources();
	}
	
	private void initResources(){
		gameAudioResource = new GameAudioResource(cyclicBarrier, game, sessionId);
		audioThread = new Thread(gameAudioResource);
		audioThread.start();
	}
	
	public void start(){
		try {
			cyclicBarrier.await();
		} catch (InterruptedException | BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
	
	public void stop(){
		try {
			gameAudioResource.stop();
			audioThread.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
