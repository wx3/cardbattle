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
import com.wx3.cardbattle.game.gameevents.BuffRecalc;
import com.wx3.cardbattle.game.gameevents.DamageEvent;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
import com.wx3.cardbattle.game.gameevents.SummonMinionEvent;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;

public class Bootstrap {
	
	private Datastore datastore;
	
	public Bootstrap(Datastore datastore) {
		this.datastore = datastore;
	}
	
	public GameInstance setup() {
		
		String s1 = "if(event.minion !== entity) {rules.damageEntity(event.minion,2)}";
		EntityRule rule1 = new EntityRule(SummonMinionEvent.class, s1, "DMG_2_SUMMONED", "Deal 2 to Summoned");
		datastore.createRule(rule1);
		
		String s2 = "if(event.entity == entity) {rules.drawCard(entity.getOwner());}";
		EntityRule rule2 = new EntityRule(DamageEvent.class, s2, "DRAW_ON_DAMAGE", "Draw on damage to this entity.");
		datastore.createRule(rule2);
		
		String s4 = "rules.damageEntity(event.getTarget(),2)";
		EntityRule rule4 = new EntityRule(PlayCardEvent.class, s4, "DMG_2_TARGET", "Deal 2 Damage to a Minion");
		datastore.createRule(rule4);
		
		EntityRule ruleBuffHealth = new EntityRule(BuffRecalc.class, "rules.buffEntity(entity, 'MAX_HEALTH', 3)", "BUFF_HEALTH_2", "+3 Health");
		datastore.createRule(ruleBuffHealth);
		
		EntityRule ruleEnchantBuff = new EntityRule(PlayCardEvent.class, "rules.enchantEntity(target, 'BUFF_HEALTH_2')", "ENCHANT_3_HEALTH" ,"Enchant an entity with +3 health");
		datastore.createRule(ruleEnchantBuff);
		
		EntityRule ruleDisenchant = new EntityRule(PlayCardEvent.class, "rules.disenchantEntity(target)", "DISENCHANT", "Disenchant an entity");
		datastore.createRule(ruleDisenchant);
		
		String v1 = "if(!target.hasTag('MINION')) error = 'Target must be minion'";
		PlayValidator minionValidator = PlayValidator.createValidator("TARGET_MINION", v1, "Validate target is minion");
		datastore.createValidator(minionValidator);

    	Card card1 = new Card("Weak Minion", "A measly little minion");
    	card1.setTag(Tag.MINION);
    	card1.getStats().put(EntityStats.ATTACK, 2);
    	card1.getStats().put(EntityStats.MAX_HEALTH, 2);
    	datastore.createCard(card1);
    	
    	Card strongMinion = new Card("Strong Minion", "A very tough minion");
    	strongMinion.setTag(Tag.MINION);
    	strongMinion.getStats().put(EntityStats.ATTACK, 3);
    	strongMinion.getStats().put(EntityStats.MAX_HEALTH, 8);
    	datastore.createCard(strongMinion);
    	
    	Card card2 = new Card("Deal 2 Minion", "Deals 2 damage to a minion");
    	card2.setTag(Tag.SPELL);
    	card2.getRules().add(rule4);
    	card2.setValidator(minionValidator);
    	datastore.createCard(card2);
    	
    	Card card3 = new Card("Damage Draw", "Draw a card whenever this minion takes damage");
    	card3.setTag(Tag.MINION);
    	card3.getStats().put(EntityStats.ATTACK, 1);
    	card3.getStats().put(EntityStats.MAX_HEALTH, 4);
    	card3.getRules().add(rule2);
    	datastore.createCard(card3);
    	
    	Card cardEnchantHealth = new Card("+3 Health", "Give a minion +3 Health");
    	cardEnchantHealth.setTag(Tag.SPELL);
    	cardEnchantHealth.getRules().add(ruleEnchantBuff);
    	cardEnchantHealth.setValidator(minionValidator);
    	datastore.createCard(cardEnchantHealth);
    	
    	Card disenchant = new Card("Disenchant", "Disenchant a minion");
    	disenchant.setTag(Tag.SPELL);
    	disenchant.getRules().add(ruleDisenchant);
    	disenchant.setValidator(minionValidator);
    	datastore.createCard(disenchant);
    	
    	List<Card> deck1 = new ArrayList<Card>();
    	deck1.add(card1);
    	deck1.add(card2);
    	deck1.add(card3);
    	deck1.add(card1);
    	deck1.add(card2);
    	deck1.add(card3);
    
    	
    	List<Card> deck2 = new ArrayList<Card>();
    	deck2.add(card1);
    	deck2.add(card2);
    	deck2.add(card3);
    	deck2.add(card1);
    	deck2.add(card2);
    	deck2.add(card3);
    	
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
