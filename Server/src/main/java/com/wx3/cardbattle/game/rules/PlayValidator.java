package com.wx3.cardbattle.game.rules;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * A rule for whether a particular card can be played, and 
 * whether the target is valid.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="validators")
public class PlayValidator {

	@Id
	private String id;
	private String description;
	private String script;
	
	public static PlayValidator createValidator(String id, String script, String description) {
		PlayValidator validator = new PlayValidator();
		validator.id = id;
		validator.script = script;
		validator.description = description;
		return validator;
	}
	
	public String getId() {
		return id;
	}
	
	public String getScript() {
		return script;
	}
	
	public String getDescription() {
		return description;
	}
	
}
