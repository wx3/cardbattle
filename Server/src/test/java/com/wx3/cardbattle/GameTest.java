package com.wx3.cardbattle;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.EntityStats;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.GameRuleEngine;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.commands.AttackCommand;
import com.wx3.cardbattle.game.commands.ChatCommand;
import com.wx3.cardbattle.game.commands.EndTurnCommand;
import com.wx3.cardbattle.game.commands.PlayCardCommand;
import com.wx3.cardbattle.game.gameevents.DamageEvent;
import com.wx3.cardbattle.game.gameevents.DrawCardEvent;
import com.wx3.cardbattle.game.gameevents.KilledEvent;
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
	protected GameRuleEngine rules;
	protected GamePlayer p1;
	protected TestMessageHandler p1handler;
	protected GamePlayer p2;
	protected TestMessageHandler p2handler;
	
	protected void setUp() {
		Datastore datastore = new Datastore();
		Bootstrap testSetup = new Bootstrap(datastore);
		game = testSetup.setup();
		game.start();
		p1 = game.getPlayerInPosition(0);
		p1handler = new TestMessageHandler();
		p1.connect(p1handler);
		p2 = game.getPlayerInPosition(1);
		p2handler = new TestMessageHandler();
		p2.connect(p2handler);
		
		rules = game.getRuleEngine();
	}
	
	/**
	 * Put a card in a player's hand as if drawn
	 * 
	 * @param card
	 * @param player
	 * @return
	 */
	private GameEntity putCardInHand(Card card, GamePlayer player) {
		GameEntity entity = rules.instantiateCard(card);
		entity.setTag(Tag.IN_HAND);
		entity.setOwner(player);
		return entity;
	}
	
	/**
	 * Play a specific named card as a player
	 * @param cardname
	 * @param player
	 */
	private GameEntity playCard(String cardname, GamePlayer player, int target) {
		Card c1 = game.getCard(cardname);
		GameEntity e1 = putCardInHand(c1, player);
		PlayCardCommand command = new PlayCardCommand(e1.getId(),target);
		player.handleCommand(command);
		return e1;
	}
	
	/**
	 * End the current player's turn.
	 * 
	 * @param player
	 */
	private void endTurn(GamePlayer player) {
		EndTurnCommand end1 = new EndTurnCommand();
		player.handleCommand(end1);
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
		Card c1 = game.getCard("Weak Minion");
		assertNotNull(c1);
		assertTrue(c1.getTags().contains(Tag.MINION));
		assertTrue(c1.getStats().containsKey(EntityStats.MAX_HEALTH));
	}
	
	/**
	 * Test card playing
	 */
	public void testPlayCard() {
		GameEntity e1 = playCard("Weak Minion", p1, 0);
		CommandResponseMessage resp = (CommandResponseMessage) p1handler.getLastMessage();
		assertTrue(resp.isSuccess());
		// We should be able to get the entity by id and it should be in play: 
		assertTrue(game.getEntity(e1.getId()).hasTag(Tag.IN_PLAY));
	}
	
	/**
	 * Test entity on entity attacking
	 */
	public void testAttack() {
		GameEntity e1 = playCard("Weak Minion", p1, 0);
		endTurn(p1);
		GameEntity e2 = playCard("Weak Minion", p2, 0);
		endTurn(p2);
		AttackCommand com1 = new AttackCommand(e1.getId(),e2.getId());
		p1.handleCommand(com1);
		assertNotNull(game.getEventHistory().stream().filter(e -> e.getClass() == DamageEvent.class).findFirst().orElse(null));
	}
	
	/**
	 * Test that validation prevents us from playing a minion targeted spell
	 * on a player entity
	 */
	public void testValidation() {
		Card c1 = game.getCard("Weak Minion");
		Card c2 = game.getCard("Deal 2 Minion");
		GameEntity e1 = putCardInHand(c1, p1);
		GameEntity e2 = putCardInHand(c2, p1);
		PlayCardCommand com1 = new PlayCardCommand(e1.getId(),0);
		p1.handleCommand(com1);
		assertTrue(e1.isInPlay());
		
		PlayCardCommand com2 = new PlayCardCommand(e2.getId(), p2.getEntity().getId());
		p1.handleCommand(com2);
		CommandResponseMessage resp = (CommandResponseMessage) p1handler.getLastMessage();
		assertFalse(resp.isSuccess());
	}
	
	/**
	 * Test that our "Damage Draw" minion draws us a card when it takes damage
	 */
	public void testDrawCardEnchantment() {
		GameEntity e1 = playCard("Damage Draw", p1, 0);
		int oldHandSize = p1.getPlayerHand().size();
		GameEntity e2 = playCard("Deal 2 Minion", p1, e1.getId());
		int newSize = p1.getPlayerHand().size();
		assertTrue(newSize > oldHandSize);
	}
	
	/**
	 * Test that disenchanting an entity removes its rules
	 */
	public void testDisenchant() {
		GameEntity e1 = playCard("Damage Draw", p1, 0);
		assertTrue(e1.getRules().size() > 0);
		playCard("Disenchant", p1, e1.getId());
		assertTrue(e1.getRules().size() == 0);
	}
	
	/**
	 * Test that we can damage and kill a minion by playing an 
	 * attack spell on it.
	 */
	public void testDamage() {
		Card c1 = game.getCard("Weak Minion");
		Card c2 = game.getCard("Deal 2 Minion");
		GameEntity e1 = putCardInHand(c1, p1);
		GameEntity e2 = putCardInHand(c2, p1);
		PlayCardCommand com1 = new PlayCardCommand(e1.getId(),0);
		p1.handleCommand(com1);
		// Make sure we can find the first card we played:
		assertNotNull(game.getEntity(e1.getId()));
		assertTrue(e1.isInPlay());
		PlayCardCommand com2 = new PlayCardCommand(e2.getId(),e1.getId());
		p1.handleCommand(com2);
		// There should be at least one damage event in the event history now:
		assertNotNull(game.getEventHistory().stream().filter(e -> e.getClass() == DamageEvent.class).findFirst().orElse(null));
		// There should be at least one killed event in the history as well:
		assertNotNull(game.getEventHistory().stream().filter(e -> e.getClass() == KilledEvent.class).findFirst().orElse(null));
	}
	
	/**
	 * Test that a minion can be enchanted with a health buff 
	 * and that subsequent disenchant behaves correctly
	 */
	public void testEnchantBuff() {
		// First get minion out there:
		GameEntity e1 = playCard("Strong Minion", p1, 0);
		// Then damage it:
		playCard("Deal 2 Minion", p1, e1.getId());
		int startCurrent = e1.getCurrentHealth();
		int startMax = e1.getMaxHealth();
		assertTrue(startMax > startCurrent);
		// Now buff it:
		playCard("+3 Health", p1, e1.getId());
		assertTrue(e1.getRules().size() > 0);
		int endCurrent = e1.getCurrentHealth();
		int endMax = e1.getMaxHealth();
		// Both start and max should be 3 higher
		assertTrue(endCurrent - startCurrent == 3);
		assertTrue(endMax - startMax == 3);
		// Now disenchant it and the max should be back to
		// start, but current is higher because the buff effectively
		// healed us:
		playCard("Disenchant", p1, e1.getId());
		assertTrue(e1.getMaxHealth() == startMax);
		assertTrue(e1.getCurrentHealth() > startCurrent);
	}
	
}
