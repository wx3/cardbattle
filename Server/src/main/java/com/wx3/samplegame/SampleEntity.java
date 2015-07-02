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

import java.util.HashMap;
import java.util.HashSet;

import com.wx3.cardbattle.game.EntityPrototype;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.messages.GameEntityView;

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
		return getStat(SampleGameInstance.COST);
	}
	
	/**
	 * How much energy does this entity have left (only applies to player
	 * entities).
	 * 
	 * @return
	 */
	public int getEnergy() {
		return getStat(SampleGameInstance.ENERGY_PER_TURN) - getVar(SampleGameInstance.ENERGY_SPENT);
	}
	
	public int getCurrentHealth() {
		return getVar(SampleGameInstance.CURRENT_HEALTH);
	}
	
	void setCurrentHealth(int health) {
		setVar(SampleGameInstance.CURRENT_HEALTH, health);
	}
	
	public int getMaxHealth() {
		return getStat(SampleGameInstance.MAX_HEALTH);
	}
	
	public void resetAttacks() {
		setVar(SampleGameInstance.ATTACKS_REMAINING, getAttacksPerTurn());
	}
	
	public int getAttacksPerTurn() {
		return getStat(SampleGameInstance.ATTACKS_PER_TURN);
	}
	
	public int getAttacksRemaining() {
		return getVar(SampleGameInstance.ATTACKS_REMAINING);
	}
	
	public void setAttacksRemaining(int val) {
		setVar(SampleGameInstance.ATTACKS_REMAINING, val);
	}
	
	public boolean isInHand() {
		return hasTag(SampleGameInstance.IN_HAND);
	}
	
	public boolean isMinion() {
		return hasTag(SampleGameInstance.MINION);
	}
	
	/**
	 * Override the default entity view to hide info about cards in the opponent's
	 * hand (besides the fact that the card is in hand).
	 * 
	 */
	@Override
	public GameEntityView getView(GamePlayer player) {
		GameEntityView view = new GameEntityView();
		view.id = getId();
		if(getOwner() != null) {
			view.ownerName = getOwner();
		}
		view.visible = true;
		view.name = name;
		if(getCreatingCard() != null) {
			view.cardId = getCreatingCard().getId();
		}
		// If the entity is in the opponent's hand, hide all stats 
		// and tags except IN_HAND:
		if(this.isInHand() && (getOwner() != player.getPlayerName())) {
			view.stats = new HashMap<String, Integer>();
			view.vars = new HashMap<String, Integer>();
			view.tags = new HashSet<String>();
			view.tags.add(SampleGameInstance.IN_HAND);
		} else {
			view.tags = new HashSet<String>(getTags());
			view.stats = getCurrentStats();
			view.vars = getCurrentVars();
		}
		return view;
	}
	
}
