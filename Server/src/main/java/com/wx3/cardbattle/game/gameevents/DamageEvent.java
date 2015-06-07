package com.wx3.cardbattle.game.gameevents;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameEntity;

/**
 * Fired whenever an entity takes damage
 * @author Kevin
 *
 */
public class DamageEvent extends GameEvent {

	@SuppressWarnings("unused")
	private int entityId;
	public int damage;
	
	@Transient
	public GameEntity entity;
	
	public DamageEvent(GameEntity entity, int damage, GameEntity cause) {
		this.entity = entity;
		this.entityId = entity.getId();
		this.damage = damage;
		if(cause != null) {
			this.cause = cause;
			this.causeId = cause.getId();
		}
	}
	
}
