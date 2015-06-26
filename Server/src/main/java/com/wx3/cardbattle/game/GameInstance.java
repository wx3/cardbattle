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
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.Timer;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.game.gameevents.EndTurnEvent;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.gameevents.StartTurnEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;
import com.wx3.cardbattle.game.messages.GameViewMessage;
import com.wx3.cardbattle.game.messages.JoinMessage;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.samplegame.commands.PlayCardCommand;
import com.wx3.samplegame.events.DrawCardEvent;
import com.wx3.samplegame.events.PlayCardEvent;
import com.wx3.samplegame.events.SummonMinionEvent;

/**
 * The game instance contains the game's state data: the current turn,
 * the entities, players, etc. Game logic is handled by the {@link RuleSystem}.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="game_instances")
public final class GameInstance<T extends GameEntity> {
	
	@Transient
	final static Logger logger = LoggerFactory.getLogger(GameInstance.class);

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created")
	private java.util.Date created = new Date();	
	
	int turn;
	
	@Transient
	private int entityIdCounter = 1;	
	
	@Transient
	private List<GamePlayer> players = new ArrayList<GamePlayer>();
	
	@Transient
	private List<T> entities = new ArrayList<T>();
	
	@Transient
	List<GameEvent> eventHistory = new ArrayList<GameEvent>();
	
	@Transient
	private RuleSystem<T> ruleSystem;
	
	@Transient
	private GameDatastore datastore;
	
	private boolean started = false;
	private boolean stopped = false;
	private boolean gameOver = false;
	
	public GameInstance(GameDatastore datastore) {
		this.datastore = datastore;
	}
	
	public long getId() {
		return id;
	}
	
	public Date getCreated() {
		return created;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	public void setRuleSystem(RuleSystem<T> rules) {
		ruleSystem = rules;
	}
	
	public RuleSystem<T> getRuleSystem() {
		return ruleSystem;
	}
	
	public boolean isStopped() {
		return stopped;
	}
	
	public void addPlayer(GamePlayer player) {
		// The player's position is equal to the size of the player list
		// prior to adding. E.g., the first player added is in position 0
		player.setPosition(players.size());
		players.add(player);
		player.setGame(this); 
		
		ruleSystem.addPlayer(player);
	}
	
	void playerConnected(GamePlayer player) {
		JoinMessage joinMessage = JoinMessage.createMessage(datastore.getCards());
		player.sendMessage(joinMessage);
		GameViewMessage viewMessage = GameViewMessage.createMessage(this, player);
		player.sendMessage(viewMessage);
	}
	
	public GamePlayer getPlayerInPosition(int pos) {
		if(pos < players.size()) {
			return players.get(pos);
		}
		return null;
	}
	
	public GamePlayer getPlayer(long id) {
		for(GamePlayer player : players) {
			if(player.getId() == id) {
				return player;
			}
		}
		return null;
	}
	
	public List<GamePlayer> getPlayers() {
		return players;
	}
	
	public EntityPrototype getCard(int cardId) {
		return datastore.getPrototype(cardId);
	}
	
	public EntityPrototype getCard(String cardName) {
		return datastore.getPrototype(cardName);
	}
	
	public EntityRule getRule(String ruleId) {
		return datastore.getRule(ruleId);
	}
	
	public void broadcastEvent(GameEvent event) {
		for(GamePlayer player : players) {
			if(player != null) {
				player.handleEvent(event);
			}
		}
	}
	
	public List<GameEvent> getEventHistory() {
		return eventHistory;
	}
	
	public void start() {
		if(ruleSystem == null) {
			throw new RuntimeException("Rule system must be defined before start.");
		}
		ruleSystem.startup();
		started = true;
	}
	
	public void stop() {
		logger.info("Game stopped.");
		stopped = true;
		for(GamePlayer player : players) {
			player.disconnect();
		}
	}
	

	public boolean isGameOver() {
		return gameOver;
	}

	void setGameOver(boolean gameOver) {
		this.gameOver = gameOver;
	}
	
	public T getEntity(int id) {
		return entities.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
	}
	
	public List<T> getEntities() {
		return new ArrayList<T>(entities);
	}
	
	public int getTurn() {
		return turn;
	}
	
 	synchronized void update() {
 		if(isStopped()) return;
 		// If the game has no connected players and is older than 30 seconds, consider it abandoned:
		Date now = new Date();
		int age = (int) ((now.getTime() - created.getTime()) / 1000);
		int connected = 0;
		for(GamePlayer player : players) {
			if(player.isConnected()) ++connected;
		}
		if(age > 30 && connected == 0){
			stop();
		}
	}
 	
 	public void registerEntity(T entity) {
 		++entityIdCounter;
 		entity.setId(entityIdCounter);
 		entities.add(entity);
 	}	
	
	boolean removeEntity(GameEntity entity) {
		return entities.remove(entity);
	}
	
	synchronized void handleCommand(GameCommand command) {
		if(!this.started) {
			throw new RuntimeException("Cannot handle command before game is started.");
		}
		command.execute();
		ruleSystem.processEvents();
		for(GamePlayer player : getPlayers()) {
			player.sendMessage(GameViewMessage.createMessage(this, player));
		}
	}
	
}
