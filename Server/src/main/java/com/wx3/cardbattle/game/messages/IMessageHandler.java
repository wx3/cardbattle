package com.wx3.cardbattle.game.messages;


/**
 * A MessageHandler provides some mechanism to communicate events to the players, 
 * e.g., sending a message over a socket or batching them up for return in the
 * next long poll. This abstracts the message transport mechanism away from the
 * game logic. 
 * 
 * @author Kevin
 *
 */
public interface IMessageHandler {

	public void handleMessage(GameMessage message);
	
}
