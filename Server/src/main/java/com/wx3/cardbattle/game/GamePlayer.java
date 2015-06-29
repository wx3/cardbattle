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
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;
import com.wx3.cardbattle.game.messages.EventMessage;
import com.wx3.cardbattle.game.messages.GameViewMessage;
import com.wx3.cardbattle.server.MessageHandler;
import com.wx3.cardbattle.server.OutboundMessage;
import com.wx3.samplegame.SampleGameRules;

/**
 * A GamePlayer is a player in a particular game (as opposed to a 
 * {@link User} which may be associated with many games). 
 * <p>
 * Player's actions are processed as {@link GameCommand} which 
 * are parsed, validated and finally executed.
 * <p>
 * Typically a player is connected to a messageHandler which sends
 * messages to a client via the network.
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name="game_players")
public final class GamePlayer {
	@Transient
	final Logger logger = LoggerFactory.getLogger(GamePlayer.class);
	
	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	private long gameId;
	private int position;
	@ManyToOne(cascade=CascadeType.ALL, optional=false)
	private User user;
	
	@ManyToMany
	@JoinTable(name="player_ingame_decks", 
		joinColumns = @JoinColumn(name="playerId"),
		inverseJoinColumns = @JoinColumn(name="cardId"))
	@OrderColumn(name="deckOrder")
	private List<EntityPrototype> playerDeck = new ArrayList<EntityPrototype>();
	
	@Transient
	private GameInstance<? extends GameEntity> game;
	
	@Transient
	private GameEntity playerEntity;
	
	/**
	 * How events get back to the player
	 */
	@Transient
	private MessageHandler messageHandler;
	
	public GamePlayer() {}
	
	public GamePlayer(User user) {
		this.user = user;
	}
	
	public long getId() {
		return id;
	}
	
	void setId(long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return user.getUsername();
	}
	
	public long getGameId() {
		return gameId;
	}

	public int getPosition() {
		return position;
	}

	void setPosition(int position) {
		this.position = position;
	}
	
	public GameInstance<? extends GameEntity> getGame() {
		return game;
	}

	void setGame(GameInstance<? extends GameEntity> game) {
		this.game = game;
		this.gameId = game.getId();
	}
	
	public void setEntity(GameEntity entity) {
		this.playerEntity = entity;
	}
	
	public GameEntity getEntity() {
		return this.playerEntity;
	}
	
	public ValidationResult handleCommand(GameCommand command) {
		command.setPlayer(this);
		ValidationResult result;
		command.parse();
		result = command.validate();
		if(result.isValid()) {
			game.getRuleSystem().handleCommand(command);
		}
		return result;
	}
	
	public void sendMessage(OutboundMessage message) {
		if(messageHandler != null) {
			messageHandler.handleMessage(message);	
		} 
	}

	public synchronized void connect(MessageHandler messageHandler) {
		if(this.game == null) {
			throw new RuntimeException("Player has no game.");
		}
		if(game.isStopped()) {
			throw new RuntimeException("Game is over.");
		}
		if(this.messageHandler != null) {
			logger.warn("Replacing existing message handler, booting old.");
			this.messageHandler.disconnect();
		}
		this.messageHandler = messageHandler;
		game.playerConnected(this);
	}
	
	public synchronized void disconnect() {
		if(messageHandler != null) {
			logger.info("Disconnecting " + this);
			messageHandler.disconnect();
			messageHandler = null;
		}
	}
	
	/**
	 * The player is considered connected if he has a message handler.
	 * 
	 * @return
	 */
	public boolean isConnected() {
		if(messageHandler != null) return true;
		return false;
	}
	
	public EntityPrototype drawCard() {
		if (playerDeck.size() > 0) {
			return playerDeck.remove(0);
		}
		return null;
	}

	public List<EntityPrototype> getPlayerDeck() {
		return playerDeck;
	}

	public void setPlayerDeck(List<EntityPrototype> playerDeck) {
		this.playerDeck = playerDeck;
	}
	
	
}
