package com.wx3.cardbattle.game.commands;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

public class EndTurnCommand extends GameCommand {

	@Override
	public CommandResponseMessage execute() {
		try {
			CommandResponseMessage response;
			game.endTurn();
			response = new CommandResponseMessage(this, true);
			return response;			
		} catch (Exception ex) {
			return new CommandResponseMessage(this, false, "Failed to process command: " + ex.getMessage());
		}
	}

}
