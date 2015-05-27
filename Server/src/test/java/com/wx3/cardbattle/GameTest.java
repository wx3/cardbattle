package com.wx3.cardbattle;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.EntityStats;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.GameRuleProcessor;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.commands.ChatCommand;
import com.wx3.cardbattle.game.commands.EndTurnCommand;
import com.wx3.cardbattle.game.commands.PlayCardCommand;
import com.wx3.cardbattle.game.gameevents.DamageEvent;
import com.wx3.cardbattle.game.gameevents.DrawCardEvent;
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
		Bootstrap testSetup = new Bootstrap(datastore);
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
	
	private GameEntity putCardInHand(Card card, GamePlayer player) {
		GameEntity entity = rules.instantiateCard(card);
		entity.setTag(Tag.IN_HAND);
		entity.setOwner(player);
		return entity;
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
	 * Test that we can get a card by name from the game
	 */
	public void testGetCard() {
		Card c1 = game.getCard("Aggro Card");
		assertTrue(c1.getRules().size() > 0);
		assertTrue(c1.getTags().contains(Tag.MINION));
		assertTrue(c1.getStats().containsKey(EntityStats.MAX_HEALTH));
	}
	
	/**
	 * Test card playing
	 */
	public void testPlayCard() {
		GameEntity e1 = rules.drawCard(p1);
		assertTrue(p1.getPlayerDeck().size() > 0);
		PlayCardCommand command = new PlayCardCommand(e1.getId(),0);
		p1.handleCommand(command);
		// We should be able to get the entity by id and it should be in play: 
		assertTrue(game.getEntity(e1.getId()).hasTag(Tag.IN_PLAY));
	}
	
	/**
	 * If an aggro card is played after an aggro card, the second aggro card 
	 * should take damage and die.
	 */
	public void testDamage() {
		Card c1 = game.getCard("Aggro Card");
		GameEntity e1 = putCardInHand(c1, p1);
		GameEntity e2 = putCardInHand(c1, p1);
		PlayCardCommand com1 = new PlayCardCommand(e1.getId(),0);
		p1.handleCommand(com1);
		// Make sure we can find the first card we played:
		assertNotNull(game.getEntity(e1.getId()));
		assertTrue(e1.isInPlay());
		PlayCardCommand com2 = new PlayCardCommand(e2.getId(),0);
		p1.handleCommand(com2);
		// The second entity should be dead now:
		assertNull(game.getEntity(e2.getId()));
		// There should be at least one damage event in the event history now:
		assertNotNull(game.getEventHistory().stream().filter(e -> e.getClass() == DamageEvent.class).findFirst().orElse(null));
	}
	
}
