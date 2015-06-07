package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.rules.EntityRule;

public class EnchantEvent extends GameEvent {
	
	public int enchantedId;
	public String ruleId;

	public EnchantEvent(GameEntity enchanted, EntityRule rule, GameEntity cause) {
		this.enchantedId = enchanted.getId();
		this.ruleId = rule.getId();
		if(cause != null) {
			this.cause = cause;
			this.causeId = cause.getId();
		}
	}
}
