package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GamePlayer;

/**
 * Event fired in response to a player sending a ChatCommand.
 * 
 * @author Kevin
 *
 */
public class ChatEvent extends GameEvent {
	
	public String username;
	public String message;
	
	public ChatEvent(String username, String message) {
		this.username = username;
		this.message = message;
	}

	@Override
	public Object getVisibleObject(GamePlayer player) {
		return this;
	}

}
