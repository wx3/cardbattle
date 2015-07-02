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

import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.samplegame.SampleEntity;
import com.wx3.samplegame.SampleGameInstance;

/**
 * Attempt to play a card, with an optional target. 
 * 
 * @author Kevin
 *
 */
public class PlayCardCommand extends SampleGameCommand {
	
	@Transient
	final static Logger logger = LoggerFactory.getLogger(PlayCardCommand.class);
	
	private int entityId;
	private int targetId;
	
	public PlayCardCommand() {}
	
	public int getEntityId() {
		return entityId;
	}
	
	public int getTargetId() {
		return targetId;
	}
	
	public PlayCardCommand(int entityId, int targetId) {
		this.entityId = entityId;
		this.targetId = targetId;
	}
	
	/**
	 * We want to validate that the card entity exists, is in our hand. If there's a target, it
	 * should be in play. Finally the game can do additional validation.
	 */
	@Override
	public ValidationResult validate(SampleGameInstance game) {
		SampleEntity cardEntity = game.getEntity(entityId);
		SampleEntity targetEntity = game.getEntity(targetId);
		
		ValidationResult result = super.validate(game);
		SampleEntity playerEntity = game.getPlayerEntity(playerName);
		if(playerEntity == null) {
			result.addError("Player has no entity.");
			return result;
		}
		if(cardEntity == null) {
			result.addError("Card entity not found.");
			return result;
		}
		if(playerEntity.getEnergy() < cardEntity.getCost()) {
			result.addError("Insufficient energy.");
		}
		if(cardEntity.getOwner() != playerName) {
			result.addError("Not your entity.");
			return result;
		}
		if(!cardEntity.hasTag(SampleGameInstance.IN_HAND)) {
			result.addError("Entity not in hand.");
			return result;
		}
		if(targetEntity != null) {
			if(!targetEntity.hasTag(SampleGameInstance.IN_PLAY)) {
				result.addError("Target not in play.");
			}
		}
		game.validatePlayCard(result, this);
		return result;
	};

	@Override
	public void execute(SampleGameInstance game) {
		SampleEntity cardEntity = game.getEntity(entityId);
		SampleEntity targetEntity = game.getEntity(targetId);
		game.playCard(cardEntity, targetEntity);
	}

}
