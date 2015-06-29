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

import com.wx3.cardbattle.ai.GameAI;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.server.OutboundMessage;
import com.wx3.samplegame.commands.EndTurnCommand;
import com.wx3.samplegame.commands.PlayCardCommand;

/**
 * @author Kevin
 *
 */
public class SampleGameAI extends GameAI {
	
	SampleGameRules rules;

	/**
	 * @param player
	 */
	public SampleGameAI(GamePlayer player) {
		super(player);
		if(player.getGame().getRuleSystem() instanceof SampleGameRules) {
			rules = (SampleGameRules) player.getGame().getRuleSystem();
		} else {
			throw new RuntimeException("Wrong rule system");
		}
	}

	@Override
	protected Collection<CommandSelection> getCommandChoices() {
		Collection<CommandSelection> choices = new ArrayList<CommandSelection>();
		Collection<SampleEntity> hand = rules.getPlayerHand(player);
		Collection<SampleEntity> entities = rules.getEntities();
		for(SampleEntity card : hand) {
			PlayCardCommand cmdNull = new PlayCardCommand(card.getId(), 0);
			cmdNull.setPlayer(player);
			ValidationResult r1 = cmdNull.validate();
			if(r1.isValid()) {
				CommandSelection selection = new CommandSelection(cmdNull, Math.random());
				choices.add(selection);
			}
			for(SampleEntity e : entities) {
				PlayCardCommand cmd = new PlayCardCommand(card.getId(), e.getId());
				cmd.setPlayer(player);
				cmd.parse();
				ValidationResult r2 = cmd.validate();
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
		@SuppressWarnings("unchecked")
		GameInstance<?> gameCopy = new GameInstance(game);
		SampleGameRules ruleCopy = new SampleGameRules((GameInstance<SampleEntity>) gameCopy);
		ruleCopy.handleCommand(command, true);
		return Math.random();
	}

	@Override
	protected GameCommand getBestCommand() {
		GameCommand choice;
		double bestVal = -1;
		CommandSelection best = null;
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
		
		return choice;
	}

}
