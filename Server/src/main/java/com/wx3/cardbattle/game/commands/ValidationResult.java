package com.wx3.cardbattle.game.commands;

import java.util.ArrayList;
import java.util.List;

/**
 * A command is validated before processing to determine
 * whether or not it is legal in the game rules. The
 * ValidationResult tells the client whether it's valid
 * and if not, why. 
 * 
 * @author Kevin
 *
 */
public class ValidationResult {

	private List<String> errors = new ArrayList<String>();
	
	public void addError(String message) {
		errors.add(message);
	}
	
	public boolean isValid() {
		if(errors.size() > 0) return false;
		return true;
	}
	
	public List<String> getErrors(){
		return errors;
	}
	
}
