package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GamePlayer;

public class StartTurnEvent extends GameEvent {

	private int turn;
	
	public StartTurnEvent(int turn) {
		this.turn = turn;
	}
	
	public int getTurn() {
		return turn;
	}
	
	@Override
	public Object getVisibleObject(GamePlayer player) {
		return this;
	}
}
