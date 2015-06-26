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
package com.wx3.cardbattle.game;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Timer;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;

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
	private Timer taskTimer;
	private UpdateGamesTask updateTask;
	private CommandFactory gameFactory;
	
	public GameServer(GameDatastore datastore, CommandFactory gameFactory) {
		this.datastore = datastore;
		this.gameFactory = gameFactory;
	}
	
	public abstract GameInstance<? extends GameEntity> createGame();
	
	public void start() {
		updateTask = new UpdateGamesTask(datastore);
		taskTimer = new Timer();
		taskTimer.schedule(updateTask, 1000, 1000);
	}
	
	public CommandFactory getGameFactory() {
		return gameFactory;
	}
	
	public GameInstance<? extends GameEntity> newGame(User user1, User user2) {
		logger.info("Creating game for " + user1 + " and " + user2);
		GameInstance<? extends GameEntity>  game = createGame();
		GamePlayer p1 = new GamePlayer(user1);
		game.addPlayer(p1);
		GamePlayer p2 = new GamePlayer(user2);
		game.addPlayer(p2);
		datastore.saveNewGame(game);
		return game;
	}
	
	public GameInstance<? extends GameEntity> getGame(long id) {
		return datastore.getGame(id);
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
			EntityPrototype card = datastore.getCard(cardName);
			deck.add(card);
		}
		
		GameInstance<? extends GameEntity>  game = newGame(user1,user2);	
		for(GamePlayer player : game.getPlayers()) {
			player.setPlayerDeck(new ArrayList<EntityPrototype>(deck));
		}
		game.start();
		return datastore.getAuthtokens(game.getId());
	}
	
	public GamePlayer authenticate(String token) throws AuthenticationException {
		return datastore.authenticate(token);
	}
	
}
