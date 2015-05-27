package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;

public class KilledEvent extends GameEvent {
	
	private int entityId;
	
	public KilledEvent(GameEntity entity) {
		this.entityId = entity.getId();
	}
	
	public int getEntityId() {
		return entityId;
	}

}
