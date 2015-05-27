package com.wx3.cardbattle.game.commands;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

public class PlayCardCommand extends GameCommand {
	
	private int entityId;
	private int targetId;
	
	private GameEntity cardEntity;
	private GameEntity targetEntity;
	
	public PlayCardCommand() {}
	
	public PlayCardCommand(int entityId, int targetId) {
		this.entityId = entityId;
		this.targetId = targetId;
	}
	
	@Override 
	public void parse() {
		cardEntity = game.getEntity(entityId);
		if(targetId > 0) {
			targetEntity = game.getEntity(targetId);
		}
	}
	
	@Override
	public void validate() throws CommandException {
		super.validate();
		
		if(cardEntity == null) {
			throw new CommandException(this, "Entity not found.");
		}
		if(cardEntity.getOwner() != getPlayer()) {
			throw new CommandException(this, "Not your entity.");
		}
		if(!cardEntity.hasTag(Tag.IN_HAND)) {
			throw new CommandException(this, "Entity not in hand.");
		}
	};

	@Override
	public CommandResponseMessage execute() {
		CommandResponseMessage response;
		try {
			game.playCard(cardEntity, targetEntity);
			response = new CommandResponseMessage(this, true);
		}
		catch (Exception ex) {
			return new CommandResponseMessage(this, false, "Failed to process command: " + ex.getMessage());
		}
		return response;
	}

}
