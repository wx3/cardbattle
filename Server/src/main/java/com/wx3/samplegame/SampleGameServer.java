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

import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.CommandFactory;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.server.GameServer;
import com.wx3.cardbattle.server.MessageHandler;

/**
 * @author Kevin
 *
 */
public class SampleGameServer extends GameServer {

	/**
	 * @param datastore
	 * @param gameFactory
	 */
	public SampleGameServer(GameDatastore datastore, CommandFactory gameFactory) {
		super(datastore, gameFactory);
	}

	@Override
	public GameInstance<? extends GameEntity> createGame() {
		GameInstance<SampleEntity> game = new GameInstance<SampleEntity>(datastore); 
		SampleGameRules rules = new SampleGameRules(game);
		game.setRuleSystem(rules);
		rules.addGlobalRules();
		return game;
	}
	
	@Override
	public void handleJsonCommand(JsonObject json, MessageHandler messageHandler) {
		String commandName = json.get("command").getAsString();
		switch(commandName) {
	    	case "testgame":
	    		handleTestGame(json, messageHandler);
	    		break;
	    	default:
	    		throw new RuntimeException("Unknown command '" + commandName + "'");
		}
	}

	private void handleTestGame(JsonObject json, MessageHandler messageHandler) {
		List<PlayerAuthtoken> authtokens = createTestGame();
    	Map<String, String> playerTokens = new HashMap<String,String>();
    	for(PlayerAuthtoken token : authtokens) {
    		playerTokens.put(token.getPlayer().getUsername(), token.getAuthtoken());
    	}
    	CreateTestGameResultMessage message = new CreateTestGameResultMessage(playerTokens);
    	messageHandler.handleMessage(message);
	}
	
	

}
