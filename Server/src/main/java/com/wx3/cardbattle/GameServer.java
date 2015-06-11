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
package com.wx3.cardbattle;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collector;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.User;

/**
 * The gameserver handles the creation of games and connecting players to
 * the appropriate game.
 * @author Kevin
 *
 */
public class GameServer {
	
	final Logger logger = LoggerFactory.getLogger(GameServer.class);

	private Datastore datastore;
	
	public GameServer(Datastore datastore) {
		this.datastore = datastore;
	}
	
	public GameInstance createGame(User user1, User user2) {
		logger.info("Creating game for " + user1 + " and " + user2);
		GameInstance  game = datastore.createGame(Arrays.asList(user1,user2));	
		game.start();
		return game;
	}
	
	public List<PlayerAuthtoken> createTestGame() {
		User user1 = datastore.getUser("goodguy");
		User user2 = datastore.getUser("badguy");
		
		Collection<Card> cards = datastore.getCards();
		logger.info("There are " + cards.size() + " cards in the game.");
		List<Card> deck1 = cards.stream()
				.sorted((e1,e2) -> Integer.compare(e1.getId(), e2.getId()))
				.collect(Collectors.toList());
		user1.setCurrentDeck(deck1);
		List<Card> deck2 = new ArrayList<Card>(deck1);
		user2.setCurrentDeck(deck2);
		
		if(user1 == null || user2 == null) {
			throw new RuntimeException("The test users 'goodguy' and 'badguy' don't exist");
		}
		GameInstance  game = datastore.createGame(Arrays.asList(user1,user2));	
		game.start();
		return datastore.getAuthtokens(game.getId());
	}
	
	public GamePlayer authenticate(String token) throws AuthenticationException {
		return datastore.authenticate(token);
	}
	
}
