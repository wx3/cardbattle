package com.wx3.cardbattle;

import java.util.Collection;
import java.util.List;

import junit.framework.TestCase;

import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.datastore.HibernateDatastore;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.game.rules.EntityRule;

public class DatastoreTest extends TestCase {
	
	GameDatastore datastore;
	
	protected void setUp() {
		datastore = new HibernateDatastore();
		Bootstrap testSetup = new Bootstrap(datastore);
		testSetup.importData("csv");
		datastore.loadCache();
		System.out.println("setup complete");
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
	 * Test that we can get a user from the datastore
	 */
	public void testGetUser() {
		User user = datastore.getUser("goodguy");
		assertNotNull(user);
	}
	
}
