package com.wx3.cardbattle;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import com.wx3.cardbattle.server.MessageHandler;
import com.wx3.cardbattle.server.OutboundMessage;

/**
 * For unit tests, there is no websocket to send messages out, so we 
 * have this TestMessageHandler so the unit tests can see what messages
 * the game generated.
 * 
 * @author Kevin
 *
 */
public class TestMessageHandler implements MessageHandler {
	
	private List<OutboundMessage> messageHistory = new ArrayList<OutboundMessage>();
	
	public void disconnect() {}

	@Override
	public void handleMessage(OutboundMessage message) {
		this.messageHistory.add(message);
	}
	
	/**
	 * Get the last message the test handler received
	 * @return
	 */
	public OutboundMessage getLastMessage() {
		return messageHistory.get(messageHistory.size() - 1);
	}
	
	/**
	 * Get the last message that's an instance of the supplied class
	 * @param clazz
	 * @return
	 */
	public OutboundMessage getLastMessage(Class<? extends OutboundMessage> clazz) {
		ListIterator<OutboundMessage> li = messageHistory.listIterator(messageHistory.size());
		while(li.hasPrevious()) {
			OutboundMessage msg = li.previous();
			if(msg.getClass() == clazz) return msg;
		}
		return null;
	}

	/**
	 * Get all the messages sent to this handler in the order they were sent.
	 * 
	 * @return
	 */
	public List<OutboundMessage> getMessages() {
		return messageHistory;
	}
}
