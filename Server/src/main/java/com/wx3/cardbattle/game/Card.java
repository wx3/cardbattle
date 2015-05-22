package com.wx3.cardbattle.game;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CollectionTable;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

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
	
	@ElementCollection
	@CollectionTable(name = "card_tags")
	private Set<String> tags = new HashSet<String>();
	
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
	
}
