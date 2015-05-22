package com.wx3.cardbattle.game.commands;

import com.google.gson.Gson;
import com.google.gson.JsonElement;

/**
 * Creates commands from a command name and JSON
 * 
 * @author Kevin
 *
 */
public class JsonCommandFactory {

	public GameCommand createCommand(String commandName, JsonElement jsonElement) {
		if(jsonElement == null) {
			throw new RuntimeException("Suppied Json cannot be null");
		}
		GameCommand command = null;
		Gson gson = new Gson();
		switch(commandName) {
			case "Chat" : command = gson.fromJson(jsonElement, ChatCommand.class);
				break;
			case "EndTurn" : command = gson.fromJson(jsonElement, EndTurnCommand.class);
				break;
			case "PlayCard" : command = gson.fromJson(jsonElement, PlayCardCommand.class);
				break;
			default : throw new RuntimeException("Invalid command '" + commandName + "'");
		}
		return command;
	}
}
