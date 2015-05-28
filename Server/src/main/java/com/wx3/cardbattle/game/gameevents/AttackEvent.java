package com.wx3.cardbattle.game.gameevents;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameEntity;

/**
 * Event fired when an entity attacks another entity.
 * 
 * @author Kevin
 *
 */
public class AttackEvent extends GameEvent {

	@SuppressWarnings("unused")
	private int attackerId;
	@SuppressWarnings("unused")
	private int targetId;
	
	@Transient
	public GameEntity attacker;
	@Transient
	public GameEntity target;
	
	public AttackEvent(GameEntity attacker, GameEntity target) {
		this.attacker = attacker;
		this.target = target;
		this.attackerId = attacker.getId();
		this.targetId = attacker.getId();
	}
}
