package com.wx3.cardbattle.game.gameevents;

import javax.persistence.Transient;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.rules.EntityRule;

/**
 * GameEvents are the observable effects of actions and rules. 
 * They can trigger {@link EntityRule} firings. They are also
 * transmitted to clients so they may update their model of 
 * the game state.  
 * 
 * @author Kevin
 *
 */
public abstract class GameEvent {

	/**
	 * If a GameEvent was caused by another entity (such as a rule
	 * firing), the cause will reference that entity.
	 */
	@Transient
	protected GameEntity cause;
	protected int causeId;
	
}
