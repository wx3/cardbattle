package com.wx3.cardbattle;

import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.datastore.Datastore;
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
	
	public GameInstance createTestGame() {
		User user1 = datastore.getUser("goodguy");
		User user2 = datastore.getUser("badguy");
		if(user1 == null || user2 == null) {
			throw new RuntimeException("The test users 'goodguy' and 'badguy' don't exist");
		}
		GameInstance  game = datastore.createGame(Arrays.asList(user1,user2));	
		game.start();
		return game;
	}
	
	public GamePlayer authenticate(String token) {
		return datastore.authenticate(token);
	}
	
}
