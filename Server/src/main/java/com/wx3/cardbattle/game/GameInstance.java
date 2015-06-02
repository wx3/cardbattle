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

import com.wx3.cardbattle.GameServer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.PlayCardCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.game.gameevents.DrawCardEvent;
import com.wx3.cardbattle.game.gameevents.EndTurnEvent;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
import com.wx3.cardbattle.game.gameevents.StartTurnEvent;
import com.wx3.cardbattle.game.gameevents.SummonMinionEvent;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;
import com.wx3.cardbattle.game.rules.EntityRule;

@Entity
@Table(name="game_instances")
public class GameInstance {
	
	@Transient
	final static Logger logger = LoggerFactory.getLogger(GameInstance.class);
	
	private static final int MAX_EVENTS = 1000;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	private long id;
	
	@Temporal(TemporalType.TIMESTAMP)
	@Column(name="created", insertable=false, updatable=false, columnDefinition="timestamp default current_timestamp")
	private java.util.Date created;	
	
	private int turn;
	
	/**
	 * String containing the script to run at game start
	 */
	private String startupScript;
	
	@Transient
	private int entityIdCounter = 1;
	
	@Transient
	private Map<Integer, Card> cardsById = new HashMap<Integer, Card>();
	
	@Transient
	private Map<String, Card> cardsByName = new HashMap<String, Card>();
	
	@Transient
	private Map<String, EntityRule> rulesById = new HashMap<String, EntityRule>();
	
	@Transient
	private List<GamePlayer> players = new ArrayList<GamePlayer>();
	
	@Transient
	private List<GameEntity> entities = new ArrayList<GameEntity>();
	
	@Transient
	private Set<GameEntity> markedForRemoval = new HashSet<GameEntity>();
	
	@Transient
	private Queue<GameEvent> eventQueue = new ConcurrentLinkedQueue<GameEvent>();
	
	@Transient
	private List<GameEvent> eventHistory = new ArrayList<GameEvent>();
	
	@Transient
	private GameUpdateTask updateTask;
	
	@Transient
	private GameRuleEngine ruleEngine;
	private boolean started = false;
	
	public long getId() {
		return id;
	}
	
	public Date getCreated() {
		return created;
	}
	
	public String getStartupScript() {
		return startupScript;
	}

	public void setCreated(Date created) {
		this.created = created;
	}
	
	public GameRuleEngine getRuleEngine() {
		return ruleEngine;
	}
	
	public void setCards(Collection<Card> cards) {
		this.cardsById = new HashMap<Integer, Card>();
		this.cardsByName = new HashMap<String, Card>();
		for(Card card : cards) {
			if(cardsById.containsKey(card.getId())) {
				logger.warn("Duplicate card id: " + card.getId());
			}
			cardsById.put(card.getId(), card);
			if(cardsByName.containsKey(card.getName())) {
				logger.warn("Duplicate card name: " + card.getName());
			}
			cardsByName.put(card.getName(), card);
		}
	}
	
	public void setRules(Collection<EntityRule> rules) {
		for(EntityRule rule : rules) {
			rulesById.put(rule.getId(), rule);
		}
	}
	
	public void addPlayer(GamePlayer player) {
		// The player's position is equal to the size of the player list
		// prior to adding. E.g., the first player added is in position 0
		player.setPosition(players.size());
		players.add(player);
		player.setGame(this); 
		
		// Create an entity for the player. 
		GameEntity playerEntity = spawnEntity();
		playerEntity.name = player.getUsername() + "_" + playerEntity.getId();
		playerEntity.setTag(Tag.PLAYER);
		playerEntity.setTag(Tag.IN_PLAY);
		playerEntity.setOwner(player);
		String script = "if(entity.getOwner() == rules.getCurrentPlayer(event.getTurn())) {"
				+ "rules.drawCard(entity.getOwner());"
				+ "rules.drawCard(entity.getOwner());"
				+ "rules.drawCard(entity.getOwner());"
				+ "}";
		EntityRule drawRule = new EntityRule(StartTurnEvent.class, script, "PLAYER_DRAW", "Player draws at start of turn.");
		playerEntity.addRule(drawRule);
		
		player.setEntity(playerEntity);
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
	
	public Card getCard(int cardId) {
		return cardsById.get(cardId);
	}
	
	public Card getCard(String cardName) {
		return cardsByName.get(cardName);
	}
	
	public EntityRule getRule(String ruleId) {
		return rulesById.get(ruleId);
	}
	
	public void addEvent(GameEvent event) {
		logger.info("Adding event " + event);
		eventQueue.add(event);
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
		ruleEngine = new GameRuleEngine(this);
		
		Timer taskTimer = new Timer();
		updateTask = new GameUpdateTask(this);
		taskTimer.schedule(updateTask, 0, 1000);
		
		ruleEngine.startup();
		
		startTurn();
		processEvents();
		started = true;
	}
	
	public GameEntity getEntity(int id) {
		return entities.stream().filter(e -> e.getId() == id).findFirst().orElse(null);
	}
	
	public List<GameEntity> getEntities() {
		return new ArrayList<GameEntity>(entities);
	}
	
	public int getTurn() {
		return turn;
	}
	
	public void endTurn() {
		addEvent(new EndTurnEvent(turn, entities.size()));
		++turn;
		startTurn();
	}
	
	public void validatePlay(ValidationResult result, PlayCardCommand command) {
		ruleEngine.validatePlay(result, command);	
	}
	
	/**
	 * Play a card onto the board with an optional targetEntity
	 * 
	 * @param cardEntity
	 */
	public void playCard(GameEntity cardEntity, GameEntity targetEntity) {
		String msg = "Playing " + cardEntity;
		if(targetEntity != null) msg += " on " + targetEntity;
		logger.info(msg);
		cardEntity.setTag(Tag.IN_PLAY);
		cardEntity.clearTag(Tag.IN_HAND);
		PlayCardEvent event = new PlayCardEvent(cardEntity, targetEntity);
		addEvent(event);
		if(cardEntity.hasTag(Tag.MINION)) {
			addEvent(new SummonMinionEvent(cardEntity));
		}
		else {
			removeEntity(cardEntity);
		}
	}
	
 	synchronized void update() {
		
	}
	
	void startTurn() {
		addEvent(new StartTurnEvent(turn));
	}
	
	GameEntity spawnEntity() {
		GameEntity entity = new GameEntity(this, entityIdCounter);
		entities.add(entity);
		++entityIdCounter;
		return entity;
	}	
	
	void removeEntity(GameEntity entity) {
		markedForRemoval.add(entity);
	}
	
	synchronized void handleCommand(GameCommand command) {
		if(!this.started) {
			throw new RuntimeException("Cannot handle command before game is started.");
		}
		command.execute();
		processEvents();
	}
	
	private void processEvents() {
		int i = 0;
		while(!eventQueue.isEmpty()) {
			GameEvent event = eventQueue.poll();
			eventHistory.add(event);
			// Iterate over a copy of entities to avoid ConcurrentModification exceptions
			// if a rule spawns an entity:
			List<GameEntity> entityList = new ArrayList<GameEntity>(entities);
			for(GameEntity entity : entityList) {
				if(entity.isInPlay()) {
					for(EntityRule rule : entity.getRules()) {
						ruleEngine.processRule(event, rule, entity);
					}
				}
			}
			// Try to remove any entities marked for removal after 
			// each event is processed:
			if(!markedForRemoval.isEmpty()) {
				for(GameEntity entity : markedForRemoval) {
					logger.info("Removing " + entity);
					if(!entities.remove(entity)) {
						logger.warn("Failed to find " + entity + " for removal");
					}
				}
				markedForRemoval.clear();
			}
			ruleEngine.recalculateStats();
			broadcastEvent(event);
			++i;
			if(i > MAX_EVENTS) {
				throw new RuntimeException("Exceeded max events: " + MAX_EVENTS);
			}
		}
	}
}
