package com.wx3.cardbattle.game.messages;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;

public class GameViewMessage extends GameMessage {
	
	private GameView game;
	
	public static GameViewMessage createMessage(GameInstance game, GamePlayer recipient) {
		GameViewMessage message = new GameViewMessage();
		message.messageClass = GameViewMessage.class.getSimpleName();
		message.game = GameView.createViewForPlayer(game, recipient);
		return message;
	}
}
