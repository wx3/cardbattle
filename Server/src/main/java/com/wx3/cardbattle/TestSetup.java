package com.wx3.cardbattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.User;

public class TestSetup {
	
	private Datastore datastore;
	
	public TestSetup(Datastore datastore) {
		this.datastore = datastore;
	}
	
	public GameInstance setup() {
		
    	Card card1 = new Card("Crappy Minion", "Ugh...");
    	card1.setTag(Tag.MINION);
    	datastore.createCard(card1);
    	Card card2 = new Card("Super Card", "What an amazing card!");
    	datastore.createCard(card2);
    	Card card3 = new Card("OK Card", "This card is alright.");
    	datastore.createCard(card3);
		
    	List<Card> deck1 = new ArrayList<Card>();
    	deck1.add(card2);
    	deck1.add(card1);
    	deck1.add(card3);
    	deck1.add(card1);
    	
    	List<Card> deck2 = new ArrayList<Card>();
    	deck2.add(card1);
    	deck2.add(card1);
    	deck2.add(card1);
    	deck2.add(card1);
    	
    	User user1 = datastore.getUser("user1");
    	if(user1 == null) {
    		user1 = new User("goodguy");
        	datastore.saveUser(user1);
    	}
    	
    	User user2 = datastore.getUser("user2");
    	if(user2 == null) {
    		user2 = new User("badguy");
        	datastore.saveUser(user2);
    	}
    	
    	user1.setCurrentDeck(deck1);
    	user2.setCurrentDeck(deck2);

    	GameInstance game = datastore.createGame(Arrays.asList(user1,user2));
    	return game;
	}
	
}
