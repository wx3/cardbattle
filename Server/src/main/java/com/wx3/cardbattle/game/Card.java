package com.wx3.cardbattle.game;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.MapKeyColumn;
import javax.persistence.OneToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import org.hibernate.annotations.Cascade;

import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;

/**
 * A Card represents a game card in a deck. Once a card is drawn into a player's
 * hand, it is represented by a {@link GameEntity}, although the GameEntity will
 * maintain a reference to creating card.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="cards")
public class Card {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String name;
	private String description;
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "card_tags")
	private Set<String> tags = new HashSet<String>();
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name = "card_stats")
	@Column(name="stat_value")
	@MapKeyColumn(name="stat_name")
	private Map<String,Integer> stats = new HashMap<String,Integer>();
	
	@ManyToMany(cascade={CascadeType.ALL}, fetch = FetchType.EAGER)
	@JoinTable(name="card_rules", 
		joinColumns = @JoinColumn(name="cardId"),
		inverseJoinColumns = @JoinColumn(name="ruleId"))
	@OrderColumn(name="ruleOrder")
	private List<EntityRule> rules = new ArrayList<EntityRule>();
	
	@ManyToOne
	@JoinColumn(name = "validator")
	private PlayValidator validator;
	
	public Card() {}
	
	public Card(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setTag(String tag) {
		tags.add(tag);
	}
	
	public Set<String> getTags() {
		return tags;
	}
	
	public Map<String,Integer> getStats() {
		return stats;
	}

	public void setStats(Map<String,Integer> stats) {
		this.stats = stats;
	}
	
	public List<EntityRule> getRules() {
		return rules;
	}
	
	public void setRules(List<EntityRule> rules) {
		this.rules = rules;
	}
	
	public PlayValidator getValidator() {
		return validator;
	}
	
	public void setValidator(PlayValidator validator) {
		this.validator = validator;
	}
	
}
