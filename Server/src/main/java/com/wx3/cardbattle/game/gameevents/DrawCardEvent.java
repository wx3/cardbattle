package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;

/**
 * A DrawCardEvent is fired whenever a player draws a card.
 * 
 * @author Kevin
 *
 */
public class DrawCardEvent extends GameEvent {

	@SuppressWarnings("unused")
	private long playerId;
	@SuppressWarnings("unused")
	private int entityId;

	public DrawCardEvent(GamePlayer player, GameEntity entity) {
		this.playerId = player.getId();
		this.entityId = entity.getId();
	}

}
