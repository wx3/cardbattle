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
import com.wx3.cardbattle.game.messages.GameMessage;
import com.wx3.cardbattle.game.messages.GameViewMessage;
import com.wx3.cardbattle.game.messages.IMessageHandler;

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
	
	@Transient
	private GameEntity playerEntity;
	
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
	
	void setId(long id) {
		this.id = id;
	}
	
	public String getUsername() {
		return user.getUsername();
	}
	
	public long getGameId() {
		return gameId;
	}

	void setGameId(long gameId) {
		this.gameId = gameId;
	}

	public int getPosition() {
		return position;
	}

	void setPosition(int position) {
		this.position = position;
	}

	void setGame(GameInstance game) {
		this.game = game;
	}
	
	void setEntity(GameEntity entity) {
		this.playerEntity = entity;
	}
	
	public GameEntity getEntity() {
		return this.playerEntity;
	}
	
	public void handleCommand(GameCommand command) {
		try {
			command.setGameInstance(game);
			command.setPlayer(this);
			CommandResponseMessage resp;
			ValidationResult result;
			command.parse();
			result = command.validate();
			if(result.isValid()) {
				game.handleCommand(command);
			}
			logger.debug("Sending command response");
			CommandResponseMessage message = new CommandResponseMessage(command, result);
			messageHandler.handleMessage(message);
		} catch(Exception ex) {
			CommandResponseMessage message = new CommandResponseMessage(command, false, "There was an error processing the command: " + ex.getMessage());
			messageHandler.handleMessage(message);
		}
	}
	
	void sendMessage(GameMessage message) {
		if(messageHandler != null) {
			messageHandler.handleMessage(message);	
		} else {
			logger.warn("Cannot send message, no handler.");
		}
	}

	void handleEvent(GameEvent event) {
		sendMessage(new EventMessage(event, this));
	}

	public synchronized void connect(IMessageHandler messageHandler) {
		if(this.game == null) {
			throw new RuntimeException("Player has no game.");
		}
		if(this.messageHandler != null) {
			logger.warn("Replacing existing message handler, booting old.");
			this.messageHandler.disconnect();
		}
		this.messageHandler = messageHandler;
		game.playerConnected(this);
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
	
	/**
	 * Returns a list of entities that a player has in play.
	 * @return
	 */
	public List<GameEntity> getPlayerInPlay() {
		List<GameEntity> inPlay = game.getEntities().stream().filter(
				e -> e.getOwner() == this && e.hasTag(Tag.IN_PLAY)
				).collect(Collectors.toList());
		return inPlay;
	}
	
	/**
	 * Can the player see the details of this entity (i.e., is the entity in the
	 * other player's hand)?
	 * 
	 * @param entity
	 * @return
	 */
	public boolean canSee(GameEntity entity) {
		if(entity.hasTag(Tag.IN_HAND) && entity.getOwner() != this) return false;
		return true;
	}
	
}
