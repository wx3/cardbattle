package com.wx3.cardbattle.game.messages;

import java.util.Map;

import com.wx3.cardbattle.game.Card;

/**
 * Message sent to the client on join, containing initial game
 * data like all the card prototypes.
 * 
 * @author Kevin
 *
 */
public class JoinMessage extends GameMessage {

	private Map<Integer, Card> cards;
	
	public static JoinMessage createMessage(Map<Integer, Card> cards) {
		JoinMessage message = new JoinMessage();
		message.cards = cards;
		message.messageClass = JoinMessage.class.getSimpleName();
		return message;
	}
	
}
