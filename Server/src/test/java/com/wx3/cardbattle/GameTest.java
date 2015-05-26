package com.wx3.cardbattle;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.GameRuleProcessor;
import com.wx3.cardbattle.game.commands.ChatCommand;
import com.wx3.cardbattle.game.commands.EndTurnCommand;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;
import com.wx3.cardbattle.game.messages.EventMessage;
import com.wx3.cardbattle.game.messages.GameMessage;

import junit.framework.TestCase;

/**
 * TestCases for game play
 * 
 * @author Kevin
 *
 */
public class GameTest extends TestCase {

	protected GameInstance game;
	protected GameRuleProcessor rules;
	protected GamePlayer p1;
	protected TestMessageHandler p1handler;
	protected GamePlayer p2;
	protected TestMessageHandler p2handler;
	
	protected void setUp() {
		Datastore datastore = new Datastore();
		TestSetup testSetup = new TestSetup(datastore);
		game = testSetup.setup();
		p1 = game.getPlayerInPosition(0);
		p1handler = new TestMessageHandler();
		p1.connect(p1handler);
		p2 = game.getPlayerInPosition(1);
		p2handler = new TestMessageHandler();
		p2.connect(p2handler);
		game.start();
		rules = game.getRules();
	}
	
	/**
	 * Test game conditions at start
	 */
	public void testGameStart() {
		assertNotNull(game);
		assertNotNull(p1);
		assertNotNull(p2);
		assertTrue(p1.getPlayerDeck().size() > 0);
		assertTrue(p2.getPlayerDeck().size() > 0);
	}
	
	/**
	 * Test that a player can send a chat message
	 */
	public void testChat() {
		ChatCommand chat = new ChatCommand();
		chat.setMessage("Hello World!");
		p1.handleCommand(chat);
		EventMessage message = (EventMessage) p2handler.getLastMessage(EventMessage.class);
		assertNotNull(message);
		assertTrue(message.getEventClass().equals("ChatEvent"));
	}
	
	/**
	 * Test end turn logic
	 */
	public void testEndTurn() {
		assertTrue(rules.getCurrentPlayer() == p1);
		// Player 2 should not be able to send an EndTurn command because it's not his turn:
		EndTurnCommand end1 = new EndTurnCommand();
		p2.handleCommand(end1);
		assertTrue(p2handler.getLastMessage() instanceof CommandResponseMessage);
		CommandResponseMessage resp1 = (CommandResponseMessage) p2handler.getLastMessage();
		assertFalse(resp1.isSuccess());
		// But Player 1 Should:
		EndTurnCommand end2 = new EndTurnCommand();
		p1.handleCommand(end2);
		CommandResponseMessage resp2 = (CommandResponseMessage) p1handler.getLastMessage();
		assertTrue(resp2.isSuccess());
	}
	
	/**
	 * Test card playing
	 */
	public void testPlayCard() {
		rules.drawCard(p1);
		assertTrue(p1.getPlayerDeck().size() > 0);
		game.playCard(p1.getPlayerHand().get(0), null);
		//assertTrue(p1.getPlayerHand().size() == 0);
	}
	
}
