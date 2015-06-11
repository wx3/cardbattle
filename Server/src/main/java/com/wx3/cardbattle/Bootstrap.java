/*******************************************************************************
 * The MIT License (MIT)
 *
 * Copyright (c) 2015 Kevin Lin
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 *******************************************************************************/
package com.wx3.cardbattle;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.EntityStats;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;

public class Bootstrap {
	
	final Logger logger = LoggerFactory.getLogger(Bootstrap.class);
	
	private Map<String, EntityRule> ruleCache = new HashMap<String, EntityRule>();
	private Map<String, PlayValidator> validatorCache = new HashMap<String, PlayValidator>();
	private Map<String, Card> cardCache = new HashMap<String, Card>();
	
	private Datastore datastore;
	
	static int parseIntOrZero(String i) {
		try {
			return Integer.parseInt(i);
		} catch (NumberFormatException e) {
			return 0;
		}
	}
	
	public Bootstrap(Datastore datastore) {
		this.datastore = datastore;
	}
	
	/**
	 * Import starting data (validators, rules, cards) into the datastore from CSV files
	 * @param folder
	 */
	public void importData(String folder) {
		try {
			importValidators(folder + "/" + "validators.csv");
			importRules(folder + "/" + "rules.csv");
			importCards(folder + "/" + "cards.csv");
		} catch (IOException ex) {
			logger.error("Failed to import data: " + ex.getMessage());
			throw new RuntimeException("Failed to import bootstrap data.", ex);
		}
	}
	
	private void importValidators(String path) throws IOException {
		Reader reader = new FileReader(path);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		for(CSVRecord record : parser) {
			String id = record.get("id");
			String description = record.get("description");
			String script = record.get("script");
			PlayValidator validator = PlayValidator.createValidator(id, script, description);
			datastore.createValidator(validator);
			validatorCache.put(id, validator);
			logger.info("Created validator " + id);
		}
		parser.close();
	}
	
	private void importRules(String path) throws IOException {
		Reader reader = new FileReader(path);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		for(CSVRecord record : parser) {
			String id = record.get("id");
			String description = record.get("description");
			String trigger = record.get("trigger");
			String script = record.get("script");
			EntityRule rule = EntityRule.createRule(trigger, script, id, description);
			datastore.createRule(rule);
			ruleCache.put(id, rule);
			logger.info("Created rule " + id);
		}
		parser.close();
	}
	
	private void importCards(String path) throws IOException {
		Reader reader = new FileReader(path);
		CSVParser parser = new CSVParser(reader, CSVFormat.EXCEL.withHeader());
		for(CSVRecord record : parser) {
			String name = record.get("name");
			String description = record.get("description");
			Set<String> tags = new HashSet<String>();
			Map<String, Integer> stats = new HashMap<String,Integer>();
			if(record.get(Tag.MINION).equals("Y")) {
				tags.add(Tag.MINION);
			}
			if(record.get(Tag.SPELL).equals("Y")) {
				tags.add(Tag.SPELL);
			}
			int maxHealth = parseIntOrZero(record.get(EntityStats.MAX_HEALTH));
			if(maxHealth > 0) {
				stats.put(EntityStats.MAX_HEALTH, maxHealth);
			}
			int attack = parseIntOrZero(record.get(EntityStats.ATTACK));
			if(attack > 0) {
				stats.put(EntityStats.ATTACK, attack);
			}
			List<EntityRule> rules = new ArrayList<EntityRule>();
			String ruleField = record.get("rules");
			String[] ruleNames = ruleField.split(",");
			for(String ruleName : ruleNames) {
				if(ruleName.length() > 0) {
					if(!ruleCache.containsKey(ruleName)) {
						throw new RuntimeException("Unable to find rule '" + ruleName + "'");
					}
					EntityRule rule = ruleCache.get(ruleName);
					rules.add(rule);
				}
			}
			String validatorName = record.get("validator");
			PlayValidator validator = null;
			if(validatorName.length() > 0) {
				if(!validatorCache.containsKey(validatorName)) {
					throw new RuntimeException("Unable to find validator '" + validatorName + "'");
				}
				validator = validatorCache.get(validatorName);
			}
			Card card = Card.createCard(name, description, tags, rules, validator, stats);
			datastore.createCard(card);
			cardCache.put(card.getName(), card);
			logger.info("Created card " + name);
		}
		parser.close();
	}
	
	public GameInstance setup() {
		if(cardCache.size() == 0) {
			throw new RuntimeException("Card cache is empty, run importData first");
		}
		/*
		String s1 = "if(event.minion !== entity) {rules.damageEntity(event.minion, 2, entity)}";
		EntityRule rule1 = new EntityRule(SummonMinionEvent.class, s1, "DMG_2_SUMMONED", "Deal 2 to Summoned");
		datastore.createRule(rule1);
		
		String s2 = "if(event.entity == entity) {rules.drawCard(entity.getOwner(), entity);}";
		EntityRule rule2 = new EntityRule(DamageEvent.class, s2, "DRAW_ON_DAMAGE", "Draw on damage to this entity.");
		datastore.createRule(rule2);
		
		String s4 = "rules.damageEntity(event.getTarget(), 2, entity)";
		EntityRule rule4 = new EntityRule(PlayCardEvent.class, s4, "DMG_2_TARGET", "Deal 2 Damage to a Minion");
		datastore.createRule(rule4);
		
		EntityRule ruleBuffHealth = new EntityRule(BuffRecalc.class, "rules.buffEntity(entity, 'MAX_HEALTH', 3)", "BUFF_HEALTH_2", "+3 Health");
		datastore.createRule(ruleBuffHealth);
		
		EntityRule ruleEnchantBuff = new EntityRule(PlayCardEvent.class, "rules.enchantEntity(target, 'BUFF_HEALTH_2', entity);"
				+ "rules.healEntity(target, 3)", "ENCHANT_3_HEALTH" ,"Enchant an entity with +3 health");
		datastore.createRule(ruleEnchantBuff);
		
		EntityRule ruleDisenchant = new EntityRule(PlayCardEvent.class, "rules.disenchantEntity(target)", "DISENCHANT", "Disenchant an entity");
		datastore.createRule(ruleDisenchant);
		
		String v1 = "if(!target || !target.hasTag('MINION')) error = 'Target must be minion'";
		PlayValidator minionValidator = PlayValidator.createValidator("TARGET_MINION", v1, "Validate target is minion");
		datastore.createValidator(minionValidator);

    	Card cardWeakMinion = new Card("Weak Minion", "A measly little minion");
    	cardWeakMinion.setTag(Tag.MINION);
    	cardWeakMinion.getStats().put(EntityStats.ATTACK, 2);
    	cardWeakMinion.getStats().put(EntityStats.MAX_HEALTH, 2);
    	datastore.createCard(cardWeakMinion);
		    	
    	Card strongMinion = new Card("Strong Minion", "A very tough minion");
    	strongMinion.setTag(Tag.MINION);
    	strongMinion.getStats().put(EntityStats.ATTACK, 3);
    	strongMinion.getStats().put(EntityStats.MAX_HEALTH, 8);
    	datastore.createCard(strongMinion);
    	
    	Card cardZap = new Card("Deal 2 Minion", "Deals 2 damage to a minion");
    	cardZap.setTag(Tag.SPELL);
    	cardZap.getRules().add(rule4);
    	cardZap.setValidator(minionValidator);
    	datastore.createCard(cardZap);
    	
    	Card cardDamDraw = new Card("Damage Draw", "Draw a card whenever this minion takes damage");
    	cardDamDraw.setTag(Tag.MINION);
    	cardDamDraw.getStats().put(EntityStats.ATTACK, 1);
    	cardDamDraw.getStats().put(EntityStats.MAX_HEALTH, 4);
    	cardDamDraw.getRules().add(rule2);
    	datastore.createCard(cardDamDraw);
    	
    	Card cardEnchantHealth = new Card("+3 Health", "Give a minion +3 Health");
    	cardEnchantHealth.setTag(Tag.SPELL);
    	cardEnchantHealth.getRules().add(ruleEnchantBuff);
    	cardEnchantHealth.setValidator(minionValidator);
    	datastore.createCard(cardEnchantHealth);
    	
    	Card disenchant = new Card("Disenchant", "Disenchant a minion");
    	disenchant.setTag(Tag.SPELL);
    	disenchant.getRules().add(ruleDisenchant);
    	disenchant.setValidator(minionValidator);
    	datastore.createCard(disenchant);*/
		
		Card cardWeakMinion = cardCache.get("Measley Minion");
		Card cardZap = cardCache.get("Zaptastic");
		Card cardBuff = cardCache.get("Health Buff +3");
		Card cardSympathy = cardCache.get("Sympathy Collector");
		Card cardStrong = cardCache.get("Strong Minion");
		Card cardDisenchant = cardCache.get("Disenchant");
		
    	List<Card> deck1 = new ArrayList<Card>();
    	deck1.add(cardWeakMinion);
    	deck1.add(cardZap);
    	deck1.add(cardSympathy);
    	deck1.add(cardWeakMinion);
    	deck1.add(cardZap);
    	deck1.add(cardStrong);
    	deck1.add(cardDisenchant);
    
    	
    	List<Card> deck2 = new ArrayList<Card>();
    	deck2.add(cardWeakMinion);
    	deck2.add(cardZap);
    	deck2.add(cardSympathy);
    	deck2.add(cardWeakMinion);
    	deck2.add(cardZap);
    	deck2.add(cardStrong);
    	deck2.add(cardDisenchant);
		
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
