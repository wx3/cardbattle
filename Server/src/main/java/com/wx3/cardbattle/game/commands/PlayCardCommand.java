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
package com.wx3.cardbattle.game.commands;

import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

/**
 * Attempt to play a card, with an optional target. 
 * 
 * @author Kevin
 *
 */
public class PlayCardCommand extends GameCommand {
	
	@Transient
	final static Logger logger = LoggerFactory.getLogger(PlayCardCommand.class);
	
	private int entityId;
	private int targetId;
	
	private GameEntity cardEntity;
	private GameEntity targetEntity;
	
	public PlayCardCommand() {}
	
	public PlayCardCommand(int entityId, int targetId) {
		this.entityId = entityId;
		this.targetId = targetId;
	}
	
	public Card getCard() {
		return cardEntity.getCreatingCard();
	}
	
	public GameEntity getTarget() {
		if(targetId > 0 && targetEntity == null) {
			throw new RuntimeException("Cannot call getTarget() before parse()");
		}
		return targetEntity;
	}
	
	@Override 
	public void parse() {
		cardEntity = game.getEntity(entityId);
		if(cardEntity == null) {
			throw new RuntimeException("Could not find entity with id '" + entityId + "'");
		}
		if(targetId > 0) {
			targetEntity = game.getEntity(targetId);
			if(targetEntity == null) {
				throw new RuntimeException("Could not find target with id '" + targetId + "'");	
			}
		}
	}
	
	/**
	 * We want to validate that the card entity exists, is in our hand. If there's a target, it
	 * should be in play. Finally the game is can due additional validation.
	 */
	@Override
	public ValidationResult validate() {
		ValidationResult result = super.validate();
		if(cardEntity == null) {
			result.addError("Entity not found.");
			return result;
		}
		if(cardEntity.getOwner() != getPlayer()) {
			result.addError("Not your entity.");
			return result;
		}
		if(!cardEntity.hasTag(Tag.IN_HAND)) {
			result.addError("Entity not in hand.");
			return result;
		}
		if(targetEntity != null) {
			if(!targetEntity.hasTag(Tag.IN_PLAY)) {
				result.addError("Target not in play.");
			}
		}
		game.validatePlay(result, this);
		return result;
	};

	@Override
	public void execute() {
		game.getRuleEngine().playCard(cardEntity, targetEntity);
	}

}
