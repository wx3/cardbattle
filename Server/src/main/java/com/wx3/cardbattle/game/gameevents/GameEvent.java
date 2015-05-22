package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GamePlayer;


public abstract class GameEvent {

	public abstract Object getVisibleObject(GamePlayer player);
}
