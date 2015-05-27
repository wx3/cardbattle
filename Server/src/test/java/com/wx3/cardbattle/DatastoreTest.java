package com.wx3.cardbattle;

import java.util.Collection;

import junit.framework.TestCase;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.Card;

public class DatastoreTest extends TestCase {
	
	Datastore datastore;

	protected void setUp() {
		datastore = new Datastore();
		Bootstrap testSetup = new Bootstrap(datastore);
		testSetup.setup();
	}
	
	public void testGetCards() {
		Collection<Card> cards = datastore.getCards();
		assertTrue(cards.size() > 0);
	}
	
}
