package com.creatingskies.game.core.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.UUID;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javafx.scene.image.Image;
import javafx.scene.layout.AnchorPane;
import javafx.scene.layout.Background;
import javafx.scene.layout.BackgroundImage;
import javafx.scene.layout.BackgroundPosition;
import javafx.scene.layout.BackgroundRepeat;
import javafx.scene.layout.BackgroundSize;

import org.apache.commons.io.FileUtils;

import com.creatingskies.game.core.Game;
import com.creatingskies.game.core.TileImage;
import com.creatingskies.game.model.obstacle.Obstacle;

public class GameResourcesManager {
	
	private String sessionId;
	private Game game;
	private CyclicBarrier cyclicBarrier;
	
	private Thread gameAudioThread;
	private Thread gameWeatherAudioThread;
	private GameAudioResource gameAudioResource;
	private GameWeatherAudioResource gameWeatherAudioResource;
	
	public static void removeTmpFiles(){
		try {
			FileUtils.deleteDirectory(new File("game/tmp/"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public GameResourcesManager(final Game game, AnchorPane weatherContainer) {
		removeTmpFiles();
		cyclicBarrier = new CyclicBarrier(3);
		sessionId = UUID.randomUUID().toString();
		this.game = game;
		
		initResources();
		initWeatherImage(game, weatherContainer);
	}
	
	public Image getPlayerImage(byte[] bs,String fileExt){
		if(bs != null){
			File dir = new File("game/tmp/"+sessionId+"/player/img/");
			if(!dir.exists()){
				dir.mkdirs();
			}
			try {
				FileOutputStream outputStream = new FileOutputStream("game/tmp/"+sessionId+"/player/img/player."+fileExt);
				outputStream.write(bs);
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			try {
				return new Image(new File("game/tmp/"+sessionId+"/player/img/player."+fileExt).toURI().toURL().toString());
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return null;
	}
	
	public Image getObstacleImage(Obstacle obstacle){
		if(obstacle == null){
			return null;
		}
		
		String basedir = "game/tmp/"+sessionId+"/map/obstacle/"+obstacle.getIdNo()+"/";
		
		try{
			File dir = new File(basedir);
			if(!dir.exists()){
				dir.mkdirs();
				
				FileOutputStream outputStream = new FileOutputStream(basedir+obstacle.getImageFileName());
				outputStream.write(obstacle.getImage());
				outputStream.close();
				obstacle.setImage(null);
			}
			
			return new Image(new File(basedir+obstacle.getImageFileName()).toURI().toURL().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	public Image getTileImage(TileImage tileImage){
		if(tileImage == null){
			return null;
		}
		String basedir = "game/tmp/"+sessionId+"/map/tile/"+tileImage.getIdNo()+"/";
		
		try{
			File dir = new File(basedir);
			if(!dir.exists()){
				dir.mkdirs();
				
				FileOutputStream outputStream = new FileOutputStream(basedir+tileImage.getFileName());
				outputStream.write(tileImage.getImage());
				outputStream.close();
				
				tileImage.setImage(null);
			}
			
			return new Image(new File(basedir+tileImage.getFileName()).toURI().toURL().toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	private void initWeatherImage(final Game game, AnchorPane weatherContainer){
		if(game.getWeather() != null){
			File dir = new File("game/tmp/"+sessionId+"/weather/img/");
			if(!dir.exists()){
				dir.mkdirs();
			}
			try {
				FileOutputStream outputStream = new FileOutputStream("game/tmp/"+sessionId+"/weather/img/weather.gif");
				outputStream.write(game.getWeather().getImage());
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			
			try {
				BackgroundImage bgi = new BackgroundImage(new Image(new File("game/tmp/"+sessionId+"/weather/img/weather.gif").toURI().toURL().toString(), 0, 0, false, true)
				, BackgroundRepeat.REPEAT, BackgroundRepeat.NO_REPEAT, BackgroundPosition.CENTER, new BackgroundSize(90, BackgroundSize.AUTO, true, true, true, false));
				
				weatherContainer.setBackground(new Background(bgi));
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void initResources(){
		gameAudioResource = new GameAudioResource(cyclicBarrier, game, sessionId);
		gameAudioThread = new Thread(gameAudioResource);
		gameAudioThread.start();
		
		gameWeatherAudioResource = new GameWeatherAudioResource(cyclicBarrier, game, sessionId);
		gameWeatherAudioThread = new Thread(gameWeatherAudioResource);
		gameWeatherAudioThread.start();
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
			gameAudioThread.interrupt();
			gameWeatherAudioResource.stop();
			gameWeatherAudioThread.interrupt();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
