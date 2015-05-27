package com.wx3.cardbattle.game.gameevents;

import com.wx3.cardbattle.game.GamePlayer;

public class EndTurnEvent extends GameEvent {

	private int turn;
	private int entityCount;
	
	public EndTurnEvent(int turn, int entityCount) {
		this.turn = turn;
		this.entityCount = entityCount;
	}
	
	public int getTurn() {
		return turn;
	}

}
