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
package com.wx3.samplegame.commands;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.EntityStats;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.samplegame.SampleEntity;
import com.wx3.samplegame.SampleGameRules;

/**
 * Tell the game to attack a particular target with with a particular attacker.
 * 
 * @author Kevin
 *
 */
public class AttackCommand extends SampleGameCommand {
	
	private int attackerId;
	private int targetId;
	
	private SampleEntity attacker;
	private SampleEntity target;
	
	@Transient
	final static Logger logger = LoggerFactory.getLogger(AttackCommand.class);
	
	public AttackCommand() {}
	
	public AttackCommand(int attackerId, int targetId) {
		this.attackerId = attackerId;
		this.targetId = targetId;
	}

	public GameEntity getAttacker() {
		return attacker;
	}
	
	public GameEntity getTarget() {
		return target;
	}
	
	@Override 
	public void parse() {
		attacker = rules.getEntity(attackerId);
		target = rules.getEntity(targetId);
	}
	
	/**
	 * The attacker should exist, belong to the player, be in play,
	 * and have an attack value.
	 * <p>
	 * The target should exist, be in play, and have positive health.
	 */
	@Override
	public ValidationResult validate() {
		ValidationResult result = super.validate();
		if(attacker == null) {
			result.addError("Attacker not found.");
		} else {
			if(attacker.getOwner() != getPlayer()) {
				result.addError("Not your entity to attack with.");
			}
			if(!attacker.isInPlay()) {
				result.addError("Attacker not in play.");
			}
			if(attacker.getStat(SampleGameRules.ATTACK) <= 0) {
				result.addError("Attacker has no attack.");
			}
		}
		if(target == null) {
			result.addError("Target not found.");
		} else {
			if(!target.isInPlay()) {
				result.addError("Target not in play");
			}
			if(target.getCurrentHealth() <= 0) {
				result.addError("Target has no health");
			}
		}
		return result;
	}

	@Override
	public void execute() {
		rules.attack(attacker, target);
	}

}
