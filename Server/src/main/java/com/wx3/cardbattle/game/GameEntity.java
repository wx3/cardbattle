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
import com.wx3.cardbattle.game.messages.GameEntityView;
import com.wx3.cardbattle.game.rules.EntityRule;

/**
 * All game objects in a particular game implementations will be a GameEntity of the 
 * subtype for that game. Subtypes may implement convenience methods to that game. For
 * example, if the your game entities have a "MAX_HEALTH" stat, your entities may 
 * have a getMaxHealth() method that wraps getStat("MAX_HEALTH").
 * <p>
 * Entities are composed of:
 * <ul>
 * <li>Tags, which are like boolean flags that can be selected against. E.g., "MINION"</li>
 * <li>{@link EntityStats}: Named integers like MAX_HEALTH and can be buffed by rules.</li>
 * <li>Vars, which are named integers like current health. Unlike Stats, Vars are not automatically
 * recalculated for buff effects.</li>
 * <li>A collection of {@link EntityRule}s, which are triggered by {@link GameEvent}s.</li>
 * </ul>
 * @author Kevin
 *
 */
public class GameEntity {
	
	public static final String DAMAGE_TAKEN = "DAMAGE_TAKEN";
	
	public String name;

	private int id;

	private EntityPrototype prototype;
	
	private String owner;
	
	private Set<String> tags = new HashSet<String>();
	EntityStats stats = new EntityStats();
	private Map<String, Integer> vars = new HashMap<String,Integer>();
	private List<EntityRule> rules = new ArrayList<EntityRule>();
	
	private boolean removed;
	
	public GameEntity(){}
	
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
	 * Get the prototype that was copied to create this entity.
	 * 
	 * @return The EntityPrototype or null
	 */
	public EntityPrototype getCreatingCard() {
		return prototype;
	}

	/**
	 * Initialize the entity from a prototype, copying its tags, stats and rules.
	 * 
	 * @param prototype
	 */
	protected void copyFromPrototype(EntityPrototype prototype) {
		this.prototype = prototype;
		this.name = prototype.getName();
		for(String tag : prototype.getTags()) {
			setTag(tag);
		}
		Map<String, Integer> cardStats = prototype.getStats();
		for(String stat: cardStats.keySet()) {
			stats.setBase(stat, cardStats.get(stat));
		}
		for(EntityRule rule : prototype.getRules()) {
			rules.add(new EntityRule(rule));
		}
		rules = new ArrayList<EntityRule>(prototype.getRules());
		stats.reset();
	}
	
	protected void copyFromEntity(GameEntity original) {
		this.prototype = original.prototype;
		this.id = original.id;
		this.name = original.name;
		this.owner = original.owner;
		this.tags = new HashSet<String>(original.tags);
		this.stats = new EntityStats(original.stats);
		this.rules = new ArrayList<EntityRule>(original.rules);
	}
	
	/**
	 * Which player, if any, does this entity below to?
	 */
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}
	
	/**
	 * Convenience method to check if the entity's owner
	 * is the provided player. 
	 * 
	 * @param player
	 * @return
	 */
	public boolean isOwnedBy(GamePlayer player) {
		if(player == null) return false;
		return player.getPlayerName().equals(owner);
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
	
	public boolean isInPlay() {
		return hasTag(GameInstance.IN_PLAY);
	}
	
	/**
	 * Entities marked for removal are removed <b>after</b> the current event has finished
	 * being processed.
	 */
	public void remove() {
		removed = true;
	}
	
	boolean isRemoved() {
		return removed;
	}
	
	/**
	 * Subclasses can override this if there are entity details that 
	 * should be hidden from a player. 
	 * 
	 * @param player
	 * @return
	 */
	public GameEntityView getView(GamePlayer player) {
		GameEntityView view = new GameEntityView();
		view.id = id;
		if(getOwner() != null) {
			view.ownerName = getOwner();
		}
		view.visible = true;
		view.name = name;
		if(getCreatingCard() != null) {
			view.cardId = getCreatingCard().getId();
		}
		view.tags = new HashSet<String>(getTags());
		view.stats = getCurrentStats();
		view.vars = getCurrentVars();
		
		return view;
	}

	@Override
	public String toString() {
		return "GameEntity(" + name + " id" + id + ")";
	}

}
