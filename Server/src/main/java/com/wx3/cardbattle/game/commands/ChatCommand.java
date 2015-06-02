package com.wx3.cardbattle.game.commands;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.gameevents.ChatEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

public class ChatCommand extends GameCommand {
	
	private String message;
	
	public ChatCommand() {}
	
	@Override
	public ValidationResult validate() {
		ValidationResult result = new ValidationResult();
		if(game == null) {
			result.addError("Game is null");
		}
		return result;
	}
	
	@Override
	public void execute() {
		ChatEvent event = new ChatEvent(player.getUsername(), message);
		game.addEvent(event);
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
