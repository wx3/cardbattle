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
package com.wx3.cardbattle.server;

import java.util.HashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.datastore.GameRecord;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.game.commands.GameCommand;

/**
 * The gameserver handles the creation of games and connecting players to
 * the appropriate game. 
 * 
 * @author Kevin
 *
 */
public abstract class GameServer {
	
	final Logger logger = LoggerFactory.getLogger(GameServer.class);

	protected GameDatastore datastore;
	
	private Map<Long, GameInstance<?>> gameInstances = new HashMap<Long, GameInstance<?>>();
	
	public GameServer(GameDatastore datastore) {
		this.datastore = datastore;
	}
	
	protected abstract GameInstance<? extends GameEntity> createGame(long id);
	
	public void start() {
	}
	
	public abstract GameCommand<?> createCommand(GamePlayer player,
			JsonObject json);
	
	public GameInstance<? extends GameEntity> newGame(User user1, User user2) {
		logger.info("Creating game for " + user1 + " and " + user2);
		GameRecord gameRecord = datastore.newGameRecord();
		GameInstance<? extends GameEntity>  game = createGame(gameRecord.getGameId());
		GamePlayer p1 = new GamePlayer(user1);
		game.addPlayer(p1);
		GamePlayer p2 = new GamePlayer(user2);
		game.addPlayer(p2);
		datastore.newAuthToken(p1, gameRecord);
		datastore.newAuthToken(p2, gameRecord);
		gameInstances.put(game.getId(), game);
		return game;
	}
	
	public GameInstance<? extends GameEntity> getGame(long id) {
		if(!gameInstances.containsKey(id)) return null;
		return gameInstances.get(id);
	}
	
	public GamePlayer authenticate(String token) throws AuthenticationException {
		if(token == null || token.length() == 0) {
			throw new AuthenticationException(AuthenticationException.NO_TOKEN);
		}
		PlayerAuthtoken authtoken = datastore.authenticate(token);
    	if(authtoken == null) {
    		throw new AuthenticationException(AuthenticationException.BAD_TOKEN);
    	}
		GameInstance<? extends GameEntity> game = getGame(authtoken.getGameId());
		if(game == null) {
			throw new AuthenticationException(AuthenticationException.MISSING_GAME);
		}
		return game.getPlayer(authtoken.getPlayerId());
	}
	
	public void handleJsonCommand(JsonObject json, MessageHandler messageHandler) {}
	
}
