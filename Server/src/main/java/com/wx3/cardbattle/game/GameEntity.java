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
package com.wx3.cardbattle.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.rules.EntityRule;

/**
 * Almost everything in the game is an entity: cards on the board, cards in a player's hand, the
 * player's character, etc. 
 * <p>
 * Entities are composed of:
 * <ul>
 * <li>Tags, which are like boolean flags that can be selected against. E.g., "MINION"</li>
 * <li>{@link EntityStats}: Named integers like MAX_HEALTH and can be buffed by rules.</li>
 * <li>Vars, which are named integers like damage taken. Unlike Stats, Vars are not automatically
 * recalculated.</li>
 * <li>A collection of {@link EntityRule}s, which are triggered by {@link GameEvent}s.</li>
 * </ul>
 * @author Kevin
 *
 */
public class GameEntity {
	
	public static final String DAMAGE_TAKEN = "DAMAGE_TAKEN";
	public static final String CURRENT_HEALTH = "CURRENT_HEALTH";
	
	public String name;

	private int id;

	private EntityPrototype card;
	
	private GamePlayer owner;
	
	private Set<String> tags = new HashSet<String>();
	EntityStats stats = new EntityStats();
	private Map<String, Integer> vars = new HashMap<String,Integer>();
	private List<EntityRule> rules = new ArrayList<EntityRule>();
	
	private GameInstance game;
	
	public GameEntity(){}
	
	public GameEntity(GameInstance game, int id) {
		this.game = game;
		this.id = id;
	}
	
	GameInstance getGame() {
		return game;
	}

	void setGame(GameInstance game) {
		this.game = game;
	}
	
	void setId(int id) {
		if(this.id > 0) {
			throw new RuntimeException("Cannot reset entity id.");
		}
		this.id = id;
	}

	/**
	 * The in-game id used to refer to the entity when communicating with clients
	 */
	public int getId() {
		return id;
	}
	
	/**
	 * What card, if any, is assocated with this entity? May or may not be the card that created the entity.
	 */
	public EntityPrototype getCreatingCard() {
		return card;
	}

	/**
	 * Initialize the entity from a card, copying its tags, stats and rules.
	 * 
	 * @param card
	 */
	void copyFromCard(EntityPrototype card) {
		this.card = card;
		this.name = card.getName();
		for(String tag : card.getTags()) {
			setTag(tag);
		}
		Map<String, Integer> cardStats = card.getStats();
		for(String stat: cardStats.keySet()) {
			stats.setBase(stat, cardStats.get(stat));
		}
		for(EntityRule rule : card.getRules()) {
			rules.add(new EntityRule(rule));
		}
		rules = new ArrayList<EntityRule>(card.getRules());
		stats.reset();
		// Do this last so max health is equal to base:
		setCurrentHealth(getMaxHealth());
	}
	
	/**
	 * Which player, if any, does this entity below to?
	 */
	public GamePlayer getOwner() {
		return owner;
	}

	public void setOwner(GamePlayer owner) {
		this.owner = owner;
	}
	
	public Collection<String> getTags() {
		return tags;
	}
	
	public void setTag(String tag) {
		tags.add(tag);
	}
	
	public void clearTag(String tag) {
		tags.remove(tag);
	}
	
	public boolean hasTag(String tag) {
		return tags.contains(tag);
	}
	
	void resetStats() {
		stats.reset();
	}
	
	public int getStat(String stat) {
		return stats.getValue(stat);
	}
	
	public void setBaseStat(String stat, int val) {
		stats.setBase(stat, val);
	}
	
	public int getBaseStat(String stat) {
		return stats.getBaseValue(stat);
	}
	
	public Map<String, Integer> getCurrentStats() {
		return stats.getCurrentValues();
	}
	
	public Map<String, Integer> getCurrentVars() {
		return Collections.unmodifiableMap(vars);
	}
	
	public int getVar(String var) {
		if(vars.containsKey(var)) {
			return vars.get(var);
		}
		return 0;
	}
	
	public void setVar(String var, int val) {
		vars.put(var, val);
	}
	
	void addRule(EntityRule rule) {
		this.rules.add(rule);
	}

	public List<EntityRule> getRules() {
		return new ArrayList<EntityRule>(rules);
	}

	void setRules(List<EntityRule> rules) {
		this.rules = rules;
	}
	
	public boolean isInHand() {
		return hasTag(Tag.IN_HAND);
	}
	
	public boolean isInPlay() {
		return hasTag(Tag.IN_PLAY);
	}
	
	public boolean isMinion() {
		return hasTag(Tag.MINION);
	}
	
	public int getCurrentHealth() {
		return getVar(CURRENT_HEALTH);
	}
	
	void setCurrentHealth(int health) {
		setVar(CURRENT_HEALTH, health);
	}
	
	public int getMaxHealth() {
		return getStat(EntityStats.MAX_HEALTH);
	}

	@Override
	public String toString() {
		return "GameEntity(" + name + " id" + id + ")";
	}

}
