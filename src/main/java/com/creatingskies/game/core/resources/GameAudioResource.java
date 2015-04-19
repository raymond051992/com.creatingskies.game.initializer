package com.creatingskies.game.core.resources;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;

import javafx.scene.media.AudioClip;

import com.creatingskies.game.core.Game;
import com.creatingskies.game.util.Logger;
import com.creatingskies.game.util.Logger.Level;

public class GameAudioResource implements GameResource{
	
	private static final String audioFileName = "gameaudio.mp3";
	private String audioDir;
	private CyclicBarrier cyclicBarrier;
	private AudioClip audioClip;
	
	public GameAudioResource(final CyclicBarrier cyclicBarrier,final Game game, final String sessionId) {
		this.cyclicBarrier = cyclicBarrier;
		if(game != null && game.getAudio() != null){
			audioDir = "game/tmp/"+sessionId+"/audio/";
			File dir = new File(audioDir);
			if(!dir.exists()){
				dir.mkdirs();
			}
			
			try {
				FileOutputStream outputStream = new FileOutputStream(audioDir+audioFileName);
				outputStream.write(game.getAudio());
				outputStream.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	@Override
	public void run() {
		try {
			cyclicBarrier.await();
			if(audioDir != null){
				audioClip = new AudioClip(new File(audioDir+audioFileName).toURI().toString());
				audioClip.setCycleCount(AudioClip.INDEFINITE);
				audioClip.play();
			}
		} catch (InterruptedException e) {
			if(Logger.isTraceEnable(getClass())){
				Logger.log(getClass(), Level.TRACE, e.getMessage(), e);
			}
		} catch (BrokenBarrierException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void stop() throws IOException {
		if(audioClip != null){
			audioClip.stop();
			audioClip.setCycleCount(0);
			audioClip = null;
		}
	}
}
