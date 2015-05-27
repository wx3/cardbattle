package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.messages.GameEntityView;

public class PlayCardEvent extends GameEvent {

	private GameEntity entity;
	private GameEntity target;
	
	public PlayCardEvent(GameEntity entity, GameEntity target) {
		this.entity = entity;
		this.target = target;
	}
	
	public GameEntity getEntity() {
		return entity;
	}
	
	public GameEntity getTarget() {
		return target;
	}
	
}
