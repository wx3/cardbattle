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
import javax.persistence.OrderColumn;
import javax.persistence.Table;

import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;

/**
 * A Card represents a game card in a deck. Once a card is drawn into a player's
 * hand, it is represented by a {@link GameEntity}, although the GameEntity will
 * maintain a reference to creating card.
 * 
 * Cards should be immutable since different references to the same card may be 
 * backed by the same underlying object.
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
	
	public static Card createCard(String name, 
			String description, 
			Collection<String> tags, 
			List<EntityRule> rules,
			PlayValidator validator,
			Map<String,Integer> stats) {
		Card card = new Card();
		card.name = name;
		card.description = description;
		card.tags = new HashSet<String>(tags);
		card.validator = validator;
		card.rules = new ArrayList<EntityRule>(rules);
		card.stats = new HashMap<String,Integer>(stats);
		return card;
	}
	
	public Card() {}
	
	public Card(String name, String description) {
		this.name = name;
		this.description = description;
	}
	
	public int getId() {
		return id;
	}
	
	public String getName() {
		return name;
	}
	
	public String getDescription() {
		return description;
	}
	
	public Set<String> getTags() {
		return tags;
	}
	
	public Map<String,Integer> getStats() {
		return stats;
	}
	
	public List<EntityRule> getRules() {
		return rules;
	}
	
	public PlayValidator getValidator() {
		return validator;
	}
	
}
