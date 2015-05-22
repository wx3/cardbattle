package com.wx3.cardbattle.game;

import java.util.TimerTask;

public class GameUpdateTask extends TimerTask {
	
	private GameInstance game;
	
	public GameUpdateTask(GameInstance game) {
		this.game = game;
	}

	@Override
	public void run() {
		game.update();
	}

}
