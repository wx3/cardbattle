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

import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.messages.GameMessage;
import com.wx3.cardbattle.game.messages.GameView;
import com.wx3.cardbattle.game.messages.GameViewMessage;
import com.wx3.cardbattle.game.messages.IMessageHandler;
import com.wx3.samplegame.commands.EndTurnCommand;

/**
 * Very simple AI for testing
 * 
 * @author Kevin
 *
 */
public class SimpleAI implements IMessageHandler {
	
	protected GameView gameView;
	protected GamePlayer player;
	
	public SimpleAI(GamePlayer player) {
		this.player = player;
	}

	@Override
	public void disconnect() {
	}

	@Override
	public void handleMessage(GameMessage message) {
		if(message instanceof GameViewMessage) {
			this.gameView = ((GameViewMessage) message).getGameView();
		}
	}
	
	public void update() {
		if(gameView == null) return;
		// Is it our turn?
		if(gameView.currentPlayer.equals(player.getUsername())) {
			GameCommand command = getBestCommand();
			player.handleCommand(command);
		}
	}
	
	protected GameCommand getBestCommand() {
		return new EndTurnCommand();
	}

}
