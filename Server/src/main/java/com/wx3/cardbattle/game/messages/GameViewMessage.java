package com.wx3.cardbattle.game.messages;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;

public class GameViewMessage extends GameMessage {
	
	private GameView game;

	public GameViewMessage(GameInstance game, GamePlayer recipient) {
		this.messageClass = this.getClass().getSimpleName();
		this.game = GameView.createViewForPlayer(game, recipient);
	}
}
