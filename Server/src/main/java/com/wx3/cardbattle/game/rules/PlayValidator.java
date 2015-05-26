package com.wx3.cardbattle.game.rules;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

/**
 * A rule for whether a particular card can be played, and 
 * whether the target is valid.
 * 
 * @author Kevin
 *
 */
@Entity
public class PlayValidator {

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private int id;
	private String name;
	private String description;
	private String script;
	
}
