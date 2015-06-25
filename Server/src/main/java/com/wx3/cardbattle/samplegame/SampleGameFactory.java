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
package com.wx3.cardbattle.samplegame;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.game.GameFactory;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.RuleSystem;
import com.wx3.cardbattle.game.commands.ChatCommand;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.samplegame.commands.AttackCommand;
import com.wx3.cardbattle.samplegame.commands.EndTurnCommand;
import com.wx3.cardbattle.samplegame.commands.PlayCardCommand;

/**
 * @author Kevin
 *
 */
public class SampleGameFactory implements GameFactory {

	private GameDatastore datastore;
	
	public SampleGameFactory(GameDatastore datastore) {
		this.datastore = datastore;
	}

	@Override
	public GameInstance createGame() {
		GameInstance game = GameInstance.createGame(datastore);
		RuleSystem rules = new SampleGameRules(game);
		game.setRuleSystem(rules);
		return game;
	}

	@Override
	public GameCommand createCommand(GamePlayer player,	JsonObject json) {
		if(json == null) {
			throw new RuntimeException("Suppied Json cannot be null");
		}
		GameCommand command = null;
		String commandName = json.get("command").getAsString();
		Gson gson = new Gson();
		switch(commandName) {
			case "Chat" : command = gson.fromJson(json, ChatCommand.class);
				break;
			case "EndTurn" : command = gson.fromJson(json, EndTurnCommand.class);
				break;
			case "PlayCard" : command = gson.fromJson(json, PlayCardCommand.class);
				break;
			case "Attack" : command = gson.fromJson(json, AttackCommand.class);
				break;
			default : throw new RuntimeException("Invalid command '" + commandName + "'");
		}
		command.setPlayer(player);
		return command;
	}

}
