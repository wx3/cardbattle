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
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentLinkedQueue;

import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonObject;
import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.gameevents.AddRuleEvent;
import com.wx3.cardbattle.game.gameevents.BuffRecalc;
import com.wx3.cardbattle.game.gameevents.ChatEvent;
import com.wx3.cardbattle.game.gameevents.EndTurnEvent;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.gameevents.RemoveRulesEvent;
import com.wx3.cardbattle.game.gameevents.StartTurnEvent;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.samplegame.events.DrawCardEvent;
import com.wx3.samplegame.events.GameOverEvent;
import com.wx3.samplegame.events.KilledEvent;

/**
 * The GameRuleEngine uses the Nashorn javascript engine to process
 * entity rules. The engine is created with a restrictive filter
 * to prevent rules from calling any Java objects/classes except
 * those supplied by the processor.
 * <p>
 * The Rule Engine also provides common utility methods for accessing 
 * game logic from scripts. 
 *  
 * @author Kevin
 *
 */
public abstract class RuleSystem<T extends GameEntity> implements CommandFactory {
	
	private transient final Logger logger = LoggerFactory.getLogger(RuleSystem.class);
	
	// This is the general rules entity:
	public static final String RULES = "RULES";
	// This entity is in play:
	public static final String IN_PLAY = "IN_PLAY";
	// Represents a player's character:
	public static final String PLAYER = "PLAYER";

	private static final int MAX_EVENTS = 1000;
	
	// We use a singleton of the script engine for performance (Nashorn seems to take up
	// a couple 100k per engine instance). Each game gets its own script context and 
	// bindings scope to avoid polluting other games. 
	private static ScriptEngine scriptEngine;
	protected ScriptContext scriptContext;
	protected Bindings scriptScope;

	protected GameInstance<T> game;
	
	private CommandFactory commandFactory;
	private Queue<GameEvent> eventQueue = new ConcurrentLinkedQueue<GameEvent>();
	private Set<GameEntity> markedForRemoval = new HashSet<GameEntity>();

	/**
	 * Don't allow scripts to access general Java classes.
	 *  
	 * @author Kevin
	 *
	 */
	static class RestrictiveFilter implements ClassFilter {

		@Override
		public boolean exposeToScripts(String s) {
			return false;
		}
	}
	
	protected static ScriptEngine getScriptEngine() {
		if(scriptEngine == null) {
			NashornScriptEngineFactory factory = new NashornScriptEngineFactory(); 
			scriptEngine = factory.getScriptEngine(new RestrictiveFilter());
			if(scriptEngine == null) {
				throw new RuntimeException("Unable to get script engine");
			}
		}
		return scriptEngine;
	}
	
	public RuleSystem(GameInstance<T> game) {
		this.game = game;
		ScriptEngine script = getScriptEngine();
		this.scriptContext = new SimpleScriptContext();
		this.scriptContext.setBindings(script.createBindings(), ScriptContext.ENGINE_SCOPE);
		this.scriptScope = this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
	}
	
	/**
	 * Because of type erasure with Java generics, we don't have a way to create
	 * a new instance of the correct GameEntity type. So game rule implementations need
	 * to explicitly instantiate entities of the correct type.
	 *  
	 * @return A new GameEntity instance of type T.
	 */
	protected abstract T createEntityInstance();
	
	//public abstract Collection<GameCommand> getValidCommands(GamePlayer player);
	
	public T spawnEntity() {
		T entity = createEntityInstance();
		game.registerEntity(entity);
		return entity;
	}
	
	public void addPlayer(GamePlayer player) {}
	
	/**
	 * Set the general game rules that are not tied to a specific entity by spawning
	 * a "rule entity" and attaching the rule set to it.
	 * 
	 * @param rules
	 */
	public void setGlobalRules(List<EntityRule> rules) {
		GameEntity ruleEntity = spawnEntity();
		ruleEntity.setRules(rules);
		ruleEntity.name = "Rule Entity";
		ruleEntity.setTag(RULES);
		ruleEntity.setTag(IN_PLAY);
	}
	
	/**
	 * Process any starting rules on the game
	 */
	void startup() {
		startTurn();
		processEvents();
	}
	
	public GameCommand createCommand(GamePlayer player, JsonObject json) {
		return commandFactory.createCommand(player, json);
	}
	
	protected void addEvent(GameEvent event) {
		logger.info("Adding event " + event);
		eventQueue.add(event);
	}
	
	protected void startTurn() {
		addEvent(new StartTurnEvent(game.turn));
	}
	
	public void endTurn() {
		addEvent(new EndTurnEvent(game.turn, game.getEntities().size()));
		++game.turn;
		startTurn();
	}
	
	public void chat(GamePlayer player, String message) {
		addEvent(new ChatEvent(player.getUsername(), message));
	}
	
	/**
	 * Returns the player for a particular turn number.  
	 * 
	 * @param turn
	 * @return
	 */
	public GamePlayer getCurrentPlayer(int turn) {
		if(game.getPlayers().size() < 1) return null;
		int i = turn % game.getPlayers().size();
		return game.getPlayers().get(i);
	}
	
	/**
	 * Return the player whose turn it currently is.
	 * @return
	 */
	public GamePlayer getCurrentPlayer() {
		return getCurrentPlayer(game.getTurn());
	}
	
	/**
	 * Get an entity by id.
	 * 
	 * @param id 	The integer id
	 * @return 		The entity corresponding to that id.
	 */
	public T getEntity(int id) {
		return game.getEntity(id);
	}
	
	/**
	 * Get all entities in the game.
	 * @return
	 */
	public Collection<T> getEntities() {
		return game.getEntities();
	}
	
	/**
	 * Add a rule to an entity by rule name. 
	 * 
	 * @param entity	The entity to add the rule to.
	 * @param ruleId	The string id corresponding to the rule.
	 * @param cause		The entity that caused the rule to be added.
	 */
	public void addRule(GameEntity entity, String ruleId, GameEntity cause) {
		EntityRule rule = game.getRule(ruleId);
		addRule(entity, rule, cause);
	}
	
	/**
	 * Add a rule to an entity.
	 * 
	 * @param entity
	 * @param rule
	 * @param cause
	 */
	public void addRule(GameEntity entity, EntityRule rule, GameEntity cause) {
		if(entity == null) {
			throw new RuleException("Entity is null");
		}
		entity.addRule(rule);
		// A rule add requires that we recalculate stats now, so that any additional 
		// actions by the same rule has the correct values. For example, a health buff
		// combined with a heal:
		recalculateStats();
		addEvent(new AddRuleEvent(entity, rule, cause));
	}
	
	/**
	 * Remove all non-permanent rules from an entity
	 * 
	 * @param entity
	 * @throws RuleException 
	 */
	public void removeRules(GameEntity entity)  {
		if(entity == null) {
			throw new RuleException("Entity is null");
		}
		List<EntityRule> rules = entity.getRules();
		Iterator<EntityRule> iter = rules.iterator();
		while(iter.hasNext()) {
			EntityRule rule = iter.next();
			if(!rule.isPermanent()) {
				iter.remove();
			}
		}
		entity.setRules(rules);
		// Like enchantment, disenchanting also requires a stat recalculation:
		recalculateStats();
		addEvent(new RemoveRulesEvent(entity, null));
	}
	
	/**
	 * Modify an entity's stat by a particular amount. Note that buffing should only 
	 * happen in response to a BuffRecalculation, which occurs after an event is 
	 * processed.
	 * 
	 * @param entity
	 * @param stat
	 * @param amount
	 */
	public void buffEntity(GameEntity entity, String stat, int amount) {
		entity.stats.buff(stat, amount);
	}
	
	
	
	/**
	 * Mark an entity for removal and fire a {@link KilledEvent}
	 * @param entity
	 */
	protected void killEntity(T entity) {
		KilledEvent event = new KilledEvent(entity);
		addEvent(event);
		removeEntity(entity);
	}
	
	/**
	 * Create a {@link GameEntity} from a {@link EntityPrototype}, acquiring the card's
	 * name, stats, tags and rules.
	 * 
	 * @param card
	 * @return
	 */
	public T instantiatePrototype(EntityPrototype card) {
		T entity = spawnEntity();
		entity.copyFromPrototype(card);
		return entity;
	}
	
	/**
	 * Mark an entity for removal without firing a {@link KilledEvent}
	 * 
	 * @param entity
	 */

	public void removeEntity(T entity) {
		markedForRemoval.add(entity);
	}
	
	/**
	 * Declare the game over.
	 */
	public void gameOver() {
		game.setGameOver(true);
	}
	
	public void trace(String message) {
		logger.info(message);
	}
	
	void processEvents() {
		int i = 0;
		while(!eventQueue.isEmpty()) {
			GameEvent event = eventQueue.poll();
			game.eventHistory.add(event);
			// Iterate over a copy of entities to avoid ConcurrentModification exceptions
			// if a rule spawns an entity:
			List<GameEntity> entityList = new ArrayList<GameEntity>(game.getEntities());
			for(GameEntity entity : entityList) {
				if(entity.isInPlay()) {
					for(EntityRule rule : entity.getRules()) {
						processRule(event, rule, entity);
					}
				}
			}
			// Try to remove any entities marked for removal after 
			// each event is processed:
			if(!markedForRemoval.isEmpty()) {
				for(GameEntity entity : markedForRemoval) {
					logger.info("Removing " + entity);
					entity.clearTag(IN_PLAY);
					if(!game.removeEntity(entity)) {
						logger.warn("Failed to find " + entity + " for removal");
					}
				}
				markedForRemoval.clear();
			}
			recalculateStats();
			game.broadcastEvent(event);
			++i;
			if(i > MAX_EVENTS) {
				throw new RuntimeException("Exceeded max events: " + MAX_EVENTS);
			}
		}
	}
	
	/**
	 * Evaluate the {@link EntityRule} attached to an entity, in the context of a 
	 * triggering event.
	 * 
	 * @param event
	 * @param rule
	 * @param entity
	 */
	private void processRule(GameEvent event, EntityRule rule, GameEntity entity) {
		try {
			if(rule.isTriggered(event)) {
				// Let the rule access the event, game and entity objects:
				scriptScope.put("event", event);
				scriptScope.put("rules", this);
				scriptScope.put("entity", entity);
				logger.debug("Executing " + rule + " for " + event + " on " + entity);
				getScriptEngine().eval(rule.getScript(),scriptContext);
			}
		} catch (final ScriptException se) {
			throw new RuntimeException("Error in rule: " + rule.getId(), se.getCause());
		} catch (Exception ex) {
			throw new RuntimeException("Unexpected error in processing rule: " + rule.getId(), ex.getCause());
		}
	}
	
	/**
	 * Resets all entities' stats to their base values, then evaluates
	 * all buff rules to update them.
	 */
	protected void recalculateStats() {
		List<T> gameEntities = game.getEntities();
		// First, reset all stats. This sets each stat to the base value:
		for(GameEntity entity : gameEntities) {
			entity.resetStats();
		}
		// Then iterate over all entities and trigger any buff rules. These rules
		// may modify one or more entity's stats:
		for(GameEntity entity : gameEntities) {
			if(entity.isInPlay()) {
				for(EntityRule rule : entity.getRules()) {
					if(rule.getEventTrigger().equals(BuffRecalc.class.getSimpleName())) {
						scriptScope.put("rules", this);
						scriptScope.put("entity", entity);
						logger.info("Recalculating buff " + rule + " on " + entity);
						try {
							getScriptEngine().eval(rule.getScript(), scriptContext);	
						} catch (Exception ex) {
							throw new RuleException("Exception processing buff " + rule + ":" + ex.getMessage());
						}
					}
				}
			}
		}
	}

}
