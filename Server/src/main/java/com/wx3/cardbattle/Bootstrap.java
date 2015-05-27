package com.wx3.cardbattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.EntityStats;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
import com.wx3.cardbattle.game.gameevents.SummonMinionEvent;
import com.wx3.cardbattle.game.rules.EntityRule;

public class Bootstrap {
	
	private Datastore datastore;
	
	public Bootstrap(Datastore datastore) {
		this.datastore = datastore;
	}
	
	public GameInstance setup() {
		
		String s1 = "if(event.minion !== entity) {rules.damageEntity(event.minion,2)}";
		EntityRule rule1 = new EntityRule(SummonMinionEvent.class, s1, "Deal 2 to Summoned");
		datastore.createRule(rule1);
		
		String s2 = "rules.drawCard(entity.getOwner());";
		EntityRule rule2 = new EntityRule(PlayCardEvent.class, s2, "Draw on Play");
		datastore.createRule(rule2);

    	Card card1 = new Card("Aggro Card", "Deals 2 damage to any minion played");
    	card1.setTag(Tag.MINION);
    	card1.getStats().put(EntityStats.ATTACK, 1);
    	card1.getStats().put(EntityStats.MAX_HEALTH, 2);
    	card1.getRules().add(rule1);
    	datastore.createCard(card1);
    	
    	Card card2 = new Card("Super Card", "What an amazing card!");
    	card2.getRules().add(rule2);
    	datastore.createCard(card2);
    	Card card3 = new Card("OK Card", "This card is alright.");
    	datastore.createCard(card3);
		
    	List<Card> deck1 = new ArrayList<Card>();
    	deck1.add(card1);
    	deck1.add(card2);
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
