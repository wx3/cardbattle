package com.wx3.cardbattle.game;

import java.util.ArrayList;
import java.util.Collection;
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
	
	public String name;

	private int id;

	private Card card;
	
	private GamePlayer owner;
	
	private Set<String> tags = new HashSet<String>();
	private EntityStats stats = new EntityStats();
	private Map<String, Integer> vars = new HashMap<String,Integer>();
	private List<EntityRule> rules = new ArrayList<EntityRule>();
	
	private GameInstance game;
	
	public GameEntity(){}
	
	public GameEntity(GameInstance game, int id) {
		this.game = game;
		this.id = id;
	}
	
	public GameInstance getGame() {
		return game;
	}

	public void setGame(GameInstance game) {
		this.game = game;
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
	public Card getCreatingCard() {
		return card;
	}

	/**
	 * Initialize the entity from a card, copying its tags, stats and rules.
	 * 
	 * @param card
	 */
	void copyFromCard(Card card) {
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
	
	public int getStat(String stat) {
		return stats.getValue(stat);
	}
	
	public int getBaseStat(String stat) {
		return stats.getBaseValue(stat);
	}
	
	public void buffStat(String stat, int amount) {
		stats.buff(stat, amount);
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
	
	public int getMaxHealth() {
		return getStat(EntityStats.MAX_HEALTH);
	}

	@Override
	public String toString() {
		return "GameEntity(" + name + " id" + id + ")";
	}

}
