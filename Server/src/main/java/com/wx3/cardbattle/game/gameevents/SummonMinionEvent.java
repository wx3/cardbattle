package com.wx3.cardbattle.game.gameevents;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;

/**
 * Fired whenever a minion is brought into play.
 * 
 * @author Kevin
 *
 */
public class SummonMinionEvent extends GameEvent {
	
	public int entityId;
	
	@Transient
	public GameEntity minion;
	
	public SummonMinionEvent(GameEntity minion) {
		this.minion = minion;
		this.entityId = minion.getId();
	}

}
