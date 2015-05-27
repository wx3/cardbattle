package com.wx3.cardbattle.game.messages;

import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.gameevents.GameEvent;

public class EventMessage extends GameMessage {

	private String eventClass;
	private Object event;
	
	public EventMessage(GameEvent event, GamePlayer recipient) {
		this.messageClass = this.getClass().getSimpleName();
		this.eventClass = event.getClass().getSimpleName();
		this.event = event;
	}

	public String getEventClass() {
		return eventClass;
	}
	
	public Object getEvent() {
		return event;
	}

}
