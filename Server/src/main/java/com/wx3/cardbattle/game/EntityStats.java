package com.wx3.cardbattle.game;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.Transient;

/**
 * A collection of named, positive integer values, such as "MAX_HEALTH"
 * 
 * @author Kevin
 *
 */
public class EntityStats {
	
	public static final String COST = "COST";
	public static final String MAX_HEALTH = "MAX_HEALTH";
	public static final String ATTACK = "ATTACK";

	private Map<String, Integer> baseValues = new HashMap<String,Integer>();
	
	@Transient
	private Map<String, Integer> currentValues = new HashMap<String,Integer>();
	
	public int getValue(String stat) {
		if(currentValues.containsKey(stat)) {
			return currentValues.get(stat);
		}
		return 0;
	}
	
	public int getBaseValue(String stat) {
		if(baseValues.containsKey(stat)) {
			return baseValues.get(stat);
		}
		return 0;
	}
	
	public void setBase(String stat, int val) {
		if(val < 0) {
			throw new RuntimeException("Stats cannot be negative");
		}
		baseValues.put(stat, val);
	}
	
	public void buff(String stat, int amount) {
		int current = getValue(stat);
		current += amount;
		if(current < 0) current = 0;
		currentValues.put(stat, current);
	}
	
	public void reset() {
		currentValues.clear();
	}
	
}
