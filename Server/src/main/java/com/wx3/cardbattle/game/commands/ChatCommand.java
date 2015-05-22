package com.wx3.cardbattle.game.commands;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.gameevents.ChatEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

public class ChatCommand extends GameCommand {
	
	private String message;
	
	public ChatCommand() {
		
	}
	
	@Override
	public void validate() throws CommandException {
		if(game == null) {
			throw new CommandException(this, "Game is null");
		}
	}
	
	@Override
	public CommandResponseMessage execute() {
		try {
			ChatEvent event = new ChatEvent(player.getUsername(), message);
			game.addEvent(event);
			return new CommandResponseMessage(this, true);
		} catch (Exception ex) {
			return new CommandResponseMessage(this, false, "Failed to process command: " + ex.getMessage());
		}
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
