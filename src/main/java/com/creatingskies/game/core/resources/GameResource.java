package com.creatingskies.game.core.resources;

import java.io.IOException;

public interface GameResource extends Runnable{

	void stop() throws IOException;
}
