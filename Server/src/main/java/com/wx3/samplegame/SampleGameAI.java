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
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.samplegame.commands.EndTurnCommand;

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
		return choices;
	}

	@Override
	protected GameCommand getBestCommand() {
		GameCommand choice;
		int bestVal = -1;
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
