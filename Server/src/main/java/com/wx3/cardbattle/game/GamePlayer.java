package com.wx3.cardbattle.game;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.persistence.CascadeType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.JoinColumn;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.ForeignKey;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.OrderColumn;
import javax.persistence.Table;
import javax.persistence.Transient;
import javax.persistence.OrderBy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.commands.CommandException;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;
import com.wx3.cardbattle.game.messages.EventMessage;
import com.wx3.cardbattle.game.messages.GameViewMessage;
import com.wx3.cardbattle.game.messages.IMessageHandler;
import com.wx3.cardbattle.netty.WebsocketMessageHandler;

@Entity
@Table(name="game_players")
public class GamePlayer {
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
	private List<Card> playerDeck = new ArrayList<Card>();
	
	@Transient
	private GameInstance game;
	
	/**
	 * How events get back to the player
	 */
	@Transient
	private IMessageHandler messageHandler;
	
	public GamePlayer() {}
	
	public GamePlayer(User user, long gameId, int position) {
		this.user = user;
		this.setGameId(gameId);
		this.setPosition(position);
	}
	
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return user.getUsername();
	}
	
	public long getGameId() {
		return gameId;
	}

	public void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public int getPosition() {
		return position;
	}

	public void setPosition(int position) {
		this.position = position;
	}

	public void setGame(GameInstance game) {
		this.game = game;
	}
	
	public void handleCommand(GameCommand command) {
		command.setGameInstance(game);
		command.setPlayer(this);
		CommandResponseMessage resp;
		try {
			command.parse();
			command.validate();
			resp = game.handleCommand(command);
		} catch (CommandException e) {
			resp = new CommandResponseMessage(command, false, e.getMessage());
		}
		messageHandler.handleMessage(resp);
	}

	public void handleEvent(GameEvent event) {
		if(this.messageHandler != null) {
			this.messageHandler.handleMessage(new EventMessage(event, this));
		} else {
			logger.warn("Can't handle event, no handler");
		}
	}

	public void connect(IMessageHandler messageHandler) {
		if(this.game == null) {
			throw new RuntimeException("Player has no game.");
		}
		this.messageHandler = messageHandler;
		// Send the player the current state of the game:
		messageHandler.handleMessage(new GameViewMessage(this.game, this));
	}
	
	public Card drawCard() {
		if (playerDeck.size() > 0) {
			return playerDeck.remove(0);
		}
		logger.warn("Player's deck is empty, cannot draw");
		return null;
	}

	public List<Card> getPlayerDeck() {
		return playerDeck;
	}

	public void setPlayerDeck(List<Card> playerDeck) {
		this.playerDeck = playerDeck;
	}
	
	/**
	 * Returns the list of entities that makes up the player's hand.
	 * @return
	 */
	public List<GameEntity> getPlayerHand() {
		List<GameEntity> hand = game.getEntities().stream().filter(
				e -> e.getOwner() == this && e.hasTag(Tag.IN_HAND)
				).collect(Collectors.toList());
		return hand;
	}
	
	public boolean canSee(GameEntity entity) {
		if(entity.hasTag(Tag.IN_HAND) && entity.getOwner() != this) return false;
		return true;
	}
	
}
