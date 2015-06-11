package com.wx3.cardbattle;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.rules.EntityRule;

public class DatastoreTest extends TestCase {
	
	Datastore datastore;
	
	protected GameInstance game;

	protected void setUp() {
		datastore = new Datastore();
		Bootstrap testSetup = new Bootstrap(datastore);
		testSetup.importData("csv");
		game = testSetup.setup();
	}
	
	/**
	 * Test that we can get a collection of cards from the datastore and that there's 
	 * at least 1 card.
	 */
	public void testGetCards() {
		Collection<Card> cards = datastore.getCards();
		assertTrue(cards.size() > 0);
	}
	
	public void testGetRules() {
		Collection<EntityRule> rules = datastore.getRules();
		assertTrue(rules.size() > 0);
		
	}
	
	/**
	 * Test that we can get a list of authtokens from the datastore and that there's
	 * exactly 2.
	 */
	public void testGetAuthtokens() {
		List<PlayerAuthtoken> authtokens = datastore.getAuthtokens(game.getId());
		assertTrue(authtokens.size() == 2);
	}
	
}
