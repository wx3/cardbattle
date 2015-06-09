package com.wx3.cardbattle.game.commands;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

public class PlayCardCommand extends GameCommand {
	
	@Transient
	final static Logger logger = LoggerFactory.getLogger(PlayCardCommand.class);
	
	private int entityId;
	private int targetId;
	
	private GameEntity cardEntity;
	private GameEntity targetEntity;
	
	public PlayCardCommand() {}
	
	public PlayCardCommand(int entityId, int targetId) {
		this.entityId = entityId;
		this.targetId = targetId;
	}
	
	public Card getCard() {
		return cardEntity.getCreatingCard();
	}
	
	public GameEntity getTarget() {
		if(targetId > 0 && targetEntity == null) {
			throw new RuntimeException("Cannot call getTarget() before parse()");
		}
		return targetEntity;
	}
	
	@Override 
	public void parse() {
		cardEntity = game.getEntity(entityId);
		if(cardEntity == null) {
			throw new RuntimeException("Could not find entity with id '" + entityId + "'");
		}
		if(targetId > 0) {
			targetEntity = game.getEntity(targetId);
			if(targetEntity == null) {
				throw new RuntimeException("Could not find target with id '" + targetId + "'");	
			}
		}
	}
	
	@Override
	public ValidationResult validate() {
		ValidationResult result = super.validate();
		if(cardEntity == null) {
			result.addError("Entity not found.");
			return result;
		}
		if(cardEntity.getOwner() != getPlayer()) {
			result.addError("Not your entity.");
			return result;
		}
		if(!cardEntity.hasTag(Tag.IN_HAND)) {
			result.addError("Entity not in hand.");
			return result;
		}
		game.validatePlay(result, this);
		return result;
	};

	@Override
	public void execute() {
		game.getRuleEngine().playCard(cardEntity, targetEntity);
	}

}
