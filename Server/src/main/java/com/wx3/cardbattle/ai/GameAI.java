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
package com.wx3.cardbattle.ai;

import java.util.Collection;

import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.messages.GameView;
import com.wx3.cardbattle.game.messages.GameViewMessage;
import com.wx3.cardbattle.game.messages.MessageHandler;
import com.wx3.cardbattle.server.OutboundMessage;
import com.wx3.samplegame.commands.EndTurnCommand;

/**
 * Abstract base class for game AIs.
 * 
 * @author Kevin
 *
 */
public abstract class GameAI {
	
	protected GamePlayer player;
	
	protected class CommandSelection {
		
		protected GameCommand command;
		public int value;
		
		public CommandSelection(GameCommand command, int value) {
			this.command = command;
			this.value = value;
		}
		
		public GameCommand getCommand() {
			return command;
		}
	}
	
	public GameAI(GamePlayer player) {
		this.player = player;
	}
	
	boolean gameOver() {
		if(player.getGame() != null) {
			return player.getGame().isGameOver();
		}
		return false;
	}
	
	protected boolean isPlayerTurn() {
		if(player.getGame() != null) {
			if(player.getGame().getRuleSystem().getCurrentPlayer() == player) {
				return true;
			}
			return false;
		}
		return false;
	}
	
	public void update() {
		if(isPlayerTurn()) {
			GameCommand command = getBestCommand();
			player.handleCommand(command);
		}
	}
	
	/**
	 * Concrete implementations should return a list of possible choices
	 * that the AI can choose from.
	 * 
	 * @return
	 */
	protected abstract Collection<CommandSelection> getCommandChoices();
	
	protected abstract GameCommand getBestCommand();

}
