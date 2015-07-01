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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonObject;
import com.wx3.cardbattle.ai.AIManager;
import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.CommandFactory;
import com.wx3.cardbattle.game.EntityPrototype;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.server.GameServer;
import com.wx3.cardbattle.server.MessageHandler;

/**
 * @author Kevin
 *
 */
public class SampleGameServer extends GameServer {
	
	private AIManager aimanager;
	
	/**
	 * @param datastore
	 * @param gameFactory
	 */
	public SampleGameServer(GameDatastore datastore, CommandFactory gameFactory) {
		super(datastore, gameFactory);
	}
	
	@Override
	public void start() {
		super.start();
		// Create a new AI manager that updates every 1 second:
		//aimanager = new AIManager(1);
		//aimanager.start();
	}

	@Override
	protected GameInstance<? extends GameEntity> createGame(long id) {
		SampleGameInstance game = new SampleGameInstance(datastore, id); 
		game.addGlobalRules();
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
    		playerTokens.put(token.getPlayerName(), token.getAuthtoken());
    	}
    	String p2Token = playerTokens.get("badguy");
    	try {
			GamePlayer p2 = authenticate(p2Token);
			SampleGameAI ai = new SampleGameAI(p2);
			p2.connect(ai);
			//aimanager.registerAI(ai);
		} catch (AuthenticationException e) {
			e.printStackTrace();
		}
    	
    	CreateTestGameResultMessage message = new CreateTestGameResultMessage(playerTokens);
    	messageHandler.handleMessage(message);
	}
	
	public List<PlayerAuthtoken> createTestGame() {
		User user1 = datastore.getUser("goodguy");
		User user2 = datastore.getUser("badguy");
		
		if(user1 == null || user2 == null) {
			throw new RuntimeException("The test users 'goodguy' and 'badguy' don't exist");
		}
		
		List<EntityPrototype> deck = new ArrayList<EntityPrototype>();
		String cardNames[] = new String[]{"Measley Minion","Zaptastic","Sympathy Collector","Health Buff +3","Strong Minion","Disenchant","Death Ray"};
		for(String cardName : cardNames) {
			EntityPrototype card = datastore.getPrototype(cardName);
			deck.add(card);
		}
		
		GameInstance<? extends GameEntity>  game = newGame(user1,user2);	
		for(GamePlayer player : game.getPlayers()) {
			player.setPlayerDeck(new ArrayList<EntityPrototype>(deck));
		}
		game.start();
		return datastore.getAuthtokens(game.getId());
	}
	
	
}
