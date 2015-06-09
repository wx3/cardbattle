package com.wx3.cardbattle.game.commands;

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

public class EndTurnCommand extends GameCommand {

	@Override
	public void execute() {
		game.getRuleEngine().endTurn();
	}

}
