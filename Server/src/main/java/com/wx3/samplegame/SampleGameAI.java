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

import java.util.ArrayList;
import java.util.Collection;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.ai.GameAI;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.samplegame.commands.EndTurnCommand;
import com.wx3.samplegame.commands.PlayCardCommand;

/**
 * @author Kevin
 *
 */
public class SampleGameAI extends GameAI {
	
	final Logger logger = LoggerFactory.getLogger(SampleGameAI.class);
	
	protected SampleGameInstance game;
	
	/**
	 * @param player
	 */
	public SampleGameAI(GamePlayer player) {
		this.player = player;
		this.game = (SampleGameInstance) player.getGame();
		if(game == null) {
			throw new RuntimeException("Invalid or null gameinstance");
		}
	}

	@Override
	protected Collection<CommandSelection> getCommandChoices() {
		Collection<CommandSelection> choices = new ArrayList<CommandSelection>();
		
		Collection<SampleEntity> hand = game.getPlayerHand(player);
		Collection<SampleEntity> entities = game.getEntities();
		for(SampleEntity card : hand) {
			PlayCardCommand cmdNull = new PlayCardCommand(card.getId(), 0);
			cmdNull.setPlayer(player);
			ValidationResult r1 = cmdNull.validate(game);
			if(r1.isValid()) {
				CommandSelection selection = new CommandSelection(cmdNull, Math.random());
				choices.add(selection);
			}
			for(SampleEntity e : entities) {
				PlayCardCommand cmd = new PlayCardCommand(card.getId(), e.getId());
				cmd.setPlayer(player);
				ValidationResult r2 = cmd.validate(game);
				if(r2.isValid()) {
					double value = simulateCommand(cmd);
					CommandSelection selection = new CommandSelection(cmd, value);
					choices.add(selection);
				}
			}
		}
		
		return choices;
	}
	
	protected double simulateCommand(GameCommand command) {
		SampleGameInstance gameCopy = (SampleGameInstance) game.copy();
		
		return evaluateGame(gameCopy);
	}
	
	private double evaluateGame(SampleGameInstance game) {
		double val = 10;
		for(SampleEntity entity : game.getEntities()) {
			if(entity.isInPlay()) {
				if(entity.isOwnedBy(player)) {
					val += entity.getCurrentHealth();
					logger.info(entity + " is owned by " + player + ", adding " + entity.getCurrentHealth());
				} else {
					val -= entity.getCurrentHealth();
					logger.info(entity + " is owned by " + player + ", subtracting " + entity.getCurrentHealth());
				}
			} else {
				if(entity.isOwnedBy(player)) {
					if(entity.isInHand()) {
						val += 1;
						logger.info(entity + " is in my hand, adding " + 1);
					}
				}
			}
		}
		return val;
	}

	@Override
	public GameCommand<?> getBestCommand() {
		GameCommand<?> choice;
		double bestVal = -1;
		CommandSelection best = null;
		logger.info("Beginning command selection...");
		for(CommandSelection selection : getCommandChoices()) {
			if(selection.value > bestVal) {
				best = selection;
				bestVal = selection.value;
			}
		}
		if(best != null) {
			choice = best.getCommand();
		}
		// If we didn't find anything with positive value, the best choice is to end turn
		else {
			choice = new EndTurnCommand();
		}
		logger.info("Selection complete, chose " + choice);
		return choice;
	}

}
