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
	private int damage;
	
	@Transient
	private GameEntity entity;
	
	public DamageEvent(GameEntity entity, int damage) {
		this.entity = entity;
		this.entityId = entity.getId();
		this.damage = damage;
	}
	
	public GameEntity getEntity() {
		return entity;
	}
	
	public int getDamage() {
		return damage;
	}
	
}
