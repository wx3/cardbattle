package com.wx3.cardbattle.game.commands;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

public class AttackCommand extends GameCommand {
	
	private int attackerId;
	private int targetId;
	
	private GameEntity attacker;
	private GameEntity target;
	
	@Transient
	final static Logger logger = LoggerFactory.getLogger(AttackCommand.class);
	
	public AttackCommand() {}
	
	public AttackCommand(int attackerId, int targetId) {
		this.attackerId = attackerId;
		this.targetId = targetId;
	}
	
	public GameEntity getAttacker() {
		return attacker;
	}
	
	public GameEntity getTarget() {
		return target;
	}
	
	@Override 
	public void parse() {
		attacker = game.getEntity(attackerId);
		target = game.getEntity(targetId);
	}
	
	@Override
	public void validate() throws CommandException {
		super.validate();
	}

	@Override
	public CommandResponseMessage execute() {
		CommandResponseMessage response;
		try {
			game.getRuleEngine().attack(attacker, target);
			response = new CommandResponseMessage(this, true);
		}
		catch (Exception ex) {
			logger.warn("Failed to process command: " + ex.getMessage());
			return new CommandResponseMessage(this, false, "Failed to process command: " + ex.getMessage());
		}
		return response;
	}

}
