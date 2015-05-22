package com.wx3.cardbattle.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.wx3.cardbattle.game.rules.EntityRule;

/**
 * Almost everything in the game is an entity: cards on the board, cards in a player's hand, the
 * player's character, etc. 
 * 
 * Entities are composed of:
 * 
 * Tags, which are like boolean flags that can be selected against.
 * Stats, which are named integers like max health and can be buffed by rules
 * Rules, which can modify game play in response to events via scripting
 * 
 * @author Kevin
 *
 */
public class GameEntity {
	
	public String name;

	/**
	 * The in-game id used to refer to the entity when communicating with clients
	 */
	private int id;

	/**
	 * What card, if any, is assocated with this entity? May or may not be the card that created the entity.
	 */
	private Card card;
	
	/**
	 * Which player, if any, does this entity below to?
	 */
	private GamePlayer owner;
	
	/**
	 * What tags are applied to this entity?
	 */
	private Set<String> tags = new HashSet<>();
	private EntityStats stats = new EntityStats();
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

	public int getId() {
		return id;
	}
	
	public Card getCreatingCard() {
		return card;
	}

	void setCreatingCard(Card creatingCard) {
		this.card = creatingCard;
	}
	

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
	
	int getStat(String stat) {
		return stats.getValue(stat);
	}
	
	int getBaseStat(String stat) {
		return stats.getBaseValue(stat);
	}
	
	void buffStat(String stat, int amount) {
		stats.buff(stat, amount);
	}
	
	void addRule(EntityRule rule) {
		this.rules.add(rule);
		rule.setEntity(this);
	}

	List<EntityRule> getRules() {
		return rules;
	}

	void setRules(List<EntityRule> rules) {
		this.rules = rules;
	}


}
