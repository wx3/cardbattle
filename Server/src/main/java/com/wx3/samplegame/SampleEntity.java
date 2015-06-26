/**
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
 * 
 */
/**
 * 
 */
package com.wx3.samplegame;

import com.wx3.cardbattle.game.EntityPrototype;
import com.wx3.cardbattle.game.GameEntity;

/**
 * @author Kevin
 *
 */
public class SampleEntity extends GameEntity {
	
	/**
	 * In addition to the default copy, we want our current health 
	 * to be equal to max health.
	 */
	@Override
	protected void copyFromPrototype(EntityPrototype card) {
		super.copyFromPrototype(card);
		setCurrentHealth(getMaxHealth());
	}
	
	/**
	 * How much does this entity cost to play?
	 * @return
	 */
	public int getCost() {
		return getStat(SampleGameRules.COST);
	}
	
	public int getCurrentHealth() {
		return getVar(SampleGameRules.CURRENT_HEALTH);
	}
	
	void setCurrentHealth(int health) {
		setVar(SampleGameRules.CURRENT_HEALTH, health);
	}
	
	public int getMaxHealth() {
		return getStat(SampleGameRules.MAX_HEALTH);
	}
	
	public void resetAttacks() {
		setVar(SampleGameRules.ATTACKS_REMAINING, getAttacksPerTurn());
	}
	
	public int getAttacksPerTurn() {
		return getStat(SampleGameRules.ATTACKS_PER_TURN);
	}
	
	public int getAttacksRemaining() {
		return getVar(SampleGameRules.ATTACKS_REMAINING);
	}
	
	public void setAttacksRemaining(int val) {
		setVar(SampleGameRules.ATTACKS_REMAINING, val);
	}
	
	public boolean isInHand() {
		return hasTag(SampleGameRules.IN_HAND);
	}
	
	public boolean isMinion() {
		return hasTag(SampleGameRules.MINION);
	}
	
}
