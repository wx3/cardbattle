package com.wx3.cardbattle.game.gameevents;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.messages.GameEntityView;

public class PlayCardEvent extends GameEvent {

	@Transient
	private GameEntity entity;
	@Transient
	private GameEntity target;
	
	public int entityId;
	public int targetId;
	
	public PlayCardEvent(GameEntity entity, GameEntity target) {
		this.entity = entity;
		this.target = target;
		
		this.entityId = entity.getId();
		if(target != null) {
			this.targetId = target.getId();
		}
	}
	
	public GameEntity getEntity() {
		return entity;
	}
	
	public GameEntity getTarget() {
		return target;
	}
	
}
