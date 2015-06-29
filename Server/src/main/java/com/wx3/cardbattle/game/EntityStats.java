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

import java.util.HashMap;
import java.util.Map;
import java.util.Collections;

import javax.persistence.Transient;

/**
 * A collection of named, positive integer values, such as "MAX_HEALTH."
 * Stats have a base value and a current value, which is recalculated after
 * every event and may be modified by "Buff" rules.  
 * 
 * @author Kevin
 *
 */
public final class EntityStats {

	private Map<String, Integer> baseValues = new HashMap<String,Integer>();
	
	@Transient
	private Map<String, Integer> currentValues = new HashMap<String,Integer>();
	
	public EntityStats(){}
	
	// Copy constructor:
	public EntityStats(EntityStats original) {
		this.baseValues = new HashMap<String,Integer>(original.baseValues);
		reset();
	}
	
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
		currentValues.put(stat, val);
	}
	
	public void buff(String stat, int amount) {
		int current = getValue(stat);
		current += amount;
		if(current < 0) current = 0;
		currentValues.put(stat, current);
	}
	
	public void reset() {
		currentValues = new HashMap<String,Integer>(baseValues);
	}
	
	public Map<String, Integer> getCurrentValues() {
		return Collections.unmodifiableMap(currentValues);
	}
	
}
