package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GamePlayer;

public class EndTurnEvent extends GameEvent {

	private int turn;
	
	public EndTurnEvent(int turn) {
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
