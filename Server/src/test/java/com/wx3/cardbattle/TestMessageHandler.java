package com.wx3.cardbattle;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.wx3.cardbattle.game.messages.GameMessage;
import com.wx3.cardbattle.game.messages.IMessageHandler;

/**
 * For unit tests, there is no websocket to send messages out, so we 
 * have this TestMessageHandler
 * 
 * @author Kevin
 *
 */
public class TestMessageHandler implements IMessageHandler {
	
	private List<GameMessage> messageHistory = new ArrayList<GameMessage>();

	@Override
	public void handleMessage(GameMessage message) {
		this.messageHistory.add(message);
	}
	
	/**
	 * Get the last message the test handler received
	 * @return
	 */
	public GameMessage getLastMessage() {
		return messageHistory.get(messageHistory.size() - 1);
	}
	
	/**
	 * Get the last message that's an instance of the supplied class
	 * @param clazz
	 * @return
	 */
	public GameMessage getLastMessage(Class<? extends GameMessage> clazz) {
		ListIterator<GameMessage> li = messageHistory.listIterator(messageHistory.size());
		while(li.hasPrevious()) {
			GameMessage msg = li.previous();
			if(msg.getClass() == clazz) return msg;
		}
		return null;
	}

	public List<GameMessage> getMessages() {
		return messageHistory;
	}
}
