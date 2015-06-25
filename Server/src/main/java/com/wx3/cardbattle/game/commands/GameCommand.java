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

import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.RuleSystem;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

/**
 * A GameCommand is how a player communicates actions to the game:
 * <p>
 * <ol> 
 * <li>A command is first parsed, which converts the json data into game data (e.g., 
 * if the json refers to an entity by its id, the parsing will get the actual 
 * entity).
 * <li> Next, the command is validated because we don't trust the client.
 * <li> Finally the command is executed, usually by calling some method of the 
 * {@link RuleSystem}.
 * </ol>
 * @author Kevin
 *
 */
public abstract class GameCommand {
	
	@Transient
	protected GamePlayer player;
	
	protected int id;
	
	/**
	 * Parsing the command resolves id references. Eg., if the command refers
	 * to an entity, parsing will populate the entity property.
	 */
	public void parse() {
		
	}
	
	/**
	 * Besides making sure the player and game exist, most commands can only
	 * be executed on the player's turn, so we make this part of the default
	 * validation. 
	 * 
	 * @return
	 */
	public ValidationResult validate()  {
		ValidationResult result = new ValidationResult();
		if(player == null) {
			result.addError("Player is null");
			return result;
		}
		GameInstance game = player.getGame();
		if(game == null) {
			result.addError("Game is null");
			return result;
		}
		return result;
	}
	
	public abstract void execute(); 
	
	public int getId() {
		return id;
	}
	
	public GamePlayer getPlayer() {
		return player;
	}
	
	public void setPlayer(GamePlayer player) {
		this.player = player;
	}
}
