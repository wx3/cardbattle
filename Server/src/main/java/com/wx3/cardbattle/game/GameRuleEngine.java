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

import javax.persistence.Transient;
import javax.script.Bindings;
import javax.script.ScriptContext;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import javax.script.SimpleScriptContext;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.wx3.cardbattle.game.commands.PlayCardCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.game.gameevents.BuffRecalc;
import com.wx3.cardbattle.game.gameevents.ChatEvent;
import com.wx3.cardbattle.game.gameevents.DamageEvent;
import com.wx3.cardbattle.game.gameevents.DisenchantEvent;
import com.wx3.cardbattle.game.gameevents.DrawCardEvent;
import com.wx3.cardbattle.game.gameevents.EnchantEvent;
import com.wx3.cardbattle.game.gameevents.EndTurnEvent;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.gameevents.GameOverEvent;
import com.wx3.cardbattle.game.gameevents.KilledEvent;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
import com.wx3.cardbattle.game.gameevents.StartTurnEvent;
import com.wx3.cardbattle.game.gameevents.SummonMinionEvent;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;

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
public class GameRuleEngine {
	
	private static final int MAX_EVENTS = 1000;
	
	// We use a singleton of the script engine for performance (Nashorn seems to take up
	// a couple 100k per engine instance). Each game gets its own script context and 
	// bindings scope to avoid polluting other games. Still uses a fair amount of mem.
	private static ScriptEngine scriptEngine;
	private ScriptContext scriptContext;
	private Bindings scriptScope;
	
	final Logger logger = LoggerFactory.getLogger(GameRuleEngine.class);

	private GameInstance game;
	
	private Queue<GameEvent> eventQueue = new ConcurrentLinkedQueue<GameEvent>();
	private Set<GameEntity> markedForRemoval = new HashSet<GameEntity>();
	
	static class RestrictiveFilter implements ClassFilter {

		@Override
		public boolean exposeToScripts(String s) {
			return false;
		}
		
	}
	
	private static ScriptEngine getScriptEngine() {
		if(scriptEngine == null) {
			NashornScriptEngineFactory factory = new NashornScriptEngineFactory(); 
			scriptEngine = factory.getScriptEngine(new RestrictiveFilter());
			if(scriptEngine == null) {
				throw new RuntimeException("Unable to get script engine");
			}
		}
		return scriptEngine;
	}
	
	GameRuleEngine(GameInstance game) {
		this.game = game;
		ScriptEngine script = getScriptEngine();
		this.scriptContext = new SimpleScriptContext();
		this.scriptContext.setBindings(script.createBindings(), ScriptContext.ENGINE_SCOPE);
		this.scriptScope = this.scriptContext.getBindings(ScriptContext.ENGINE_SCOPE);
	}
	
	/**
	 * Process any starting rules on the game
	 */
	void startup() {
		startTurn();
		processEvents();
	}
	
	void validatePlay(ValidationResult result, PlayCardCommand command) {
		// If the command's card has no validator, we don't need to do anything
		if(command.getCard().getValidator() == null) return;
		try {
			scriptScope.put("command", command);
			scriptScope.put("target", command.getTarget());
			scriptScope.put("rules", this);
			scriptScope.put("error", null);
			PlayValidator validator = command.getCard().getValidator();
			getScriptEngine().eval(validator.getScript(), scriptContext);
			if(scriptScope.get("error") != null) {
				result.addError(scriptScope.get("error").toString());
			}
		} catch (final ScriptException se) {
			result.addError("Scripting exception: " + se.getMessage());
		} 
	}
	
	void addEvent(GameEvent event) {
		logger.info("Adding event " + event);
		eventQueue.add(event);
	}
	
	void startTurn() {
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
	
	
	public void enchantEntity(GameEntity entity, String ruleId, GameEntity cause) {
		if(entity == null) {
			throw new RuleException("Entity is null");
		}
		EntityRule rule = game.getRule(ruleId);
		entity.addRule(rule);
		// An enchantment requires that we recalculate stats now, so that any additional 
		// actions by the same rule has the correct values. For example, a health buff
		// combined with a heal:
		recalculateStats();
		addEvent(new EnchantEvent(entity, rule, cause));
	}
	
	/**
	 * Remove all non-permanent rules from an entity
	 * 
	 * @param entity
	 * @throws RuleException 
	 */
	public void disenchantEntity(GameEntity entity)  {
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
		addEvent(new DisenchantEvent(entity, null));
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
	 * Heal the entity by amount up to the entity's max health.
	 * 
	 * @param entity
	 * @param amount
	 */
	public void healEntity(GameEntity entity, int amount) {
		entity.setCurrentHealth(entity.getCurrentHealth() + amount);
		if(entity.getCurrentHealth() > entity.getMaxHealth()) {
			entity.setCurrentHealth(entity.getMaxHealth());
		}
	}
	
	/**
	 * Heal the entity by however much damage it's taken.
	 * @param entity
	 */
	public void healEntity(GameEntity entity) {
		int damage = entity.getMaxHealth() - entity.getCurrentHealth();
		healEntity(entity, damage);
	}
	
	/**
	 * Deliver damage from attacker to the target equal to its attack stat,
	 * and vice versa.
	 * 
	 * @param attacker
	 * @param target
	 */
	public void attack(GameEntity attacker, GameEntity target) {
		if(attacker == null) {
			throw new RuleException("Attacker is null");
		}
		if(target == null) {
			throw new RuleException("Target is null");
		}
		if(!attacker.isInPlay()) {
			throw new RuleException("Attacker is not in play");
		}
		if(!target.isInPlay()) {
			throw new RuleException("Target is not in play");
		}
		int attackerAttack = attacker.getStat(EntityStats.ATTACK);
		int targetAttack = target.getStat(EntityStats.ATTACK);
		if(attackerAttack <= 0) {
			throw new RuleException("Attacker has no attack value");
		}
		damageEntity(target, attackerAttack, attacker);
		damageEntity(attacker, targetAttack, target);
	}

	/**
	 * Deliver damage to an entity, destroying it if total
	 * damage exceeds its max health.
	 * 
	 * @param entity
	 * @param damage
	 */
	public void damageEntity(GameEntity entity, int damage, GameEntity cause) {
		if(entity == null) {
			throw new RuntimeException("Entity is null");
		}
		if(!entity.isInPlay()) {
			throw new RuntimeException("Entity is not in play");
		}
		// Cap damage at the entity's max health:
		if(damage > entity.getCurrentHealth()) {
			damage = entity.getCurrentHealth();
		}
		if(damage <= 0) return;
		int currentHealth = entity.getCurrentHealth();
		currentHealth -= damage;
		entity.setCurrentHealth(currentHealth);
		addEvent(new DamageEvent(entity, damage, cause));
		if(currentHealth <= 0) {
			killEntity(entity);
		}
	}
	
	/**
	 * Mark an entity for removal and fire a {@link KilledEvent}
	 * @param entity
	 */
	void killEntity(GameEntity entity) {
		KilledEvent event = new KilledEvent(entity);
		addEvent(event);
		game.removeEntity(entity);
	}
	
	public GameEntity drawCard(GamePlayer player) {
		return drawCard(player, null);
	}
	
	/**
	 * Draw a card for the supplied player, creating the necessary
	 * card entity.
	 * 
	 * @param player
	 * @param cause 	The GameEntity that triggered the draw
	 * @return The entity created by the draw
	 */
	public GameEntity drawCard(GamePlayer player, GameEntity cause) {
		Card card = player.drawCard();
		if(card != null) {
			GameEntity entity = instantiateCard(card);
			entity.setTag(Tag.IN_HAND);
			entity.setOwner(player);
			addEvent(new DrawCardEvent(player, entity, cause));
			return entity;
		}
		else {
			return null;
		}
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
	
	
	/**
	 * Create a {@link GameEntity} from a {@link Card}, acquiring the card's
	 * name, stats, tags and rules.
	 * 
	 * @param card
	 * @return
	 */
	public GameEntity instantiateCard(Card card) {
		GameEntity entity = game.spawnEntity();
		entity.copyFromCard(card);
		return entity;
	}
	
	/**
	 * Mark an entity for removal without firing a {@link KilledEvent}
	 * 
	 * @param entity
	 */

	public void removeEntity(GameEntity entity) {
		markedForRemoval.add(entity);
	}
	
	public void gameOver() {
		logger.info("Game over, man");
		GamePlayer winner = null;
		for(GamePlayer player : game.getPlayers()) {
			if(player.getEntity().getCurrentHealth() > 0) {
				winner = player;
			}
		}
		addEvent(new GameOverEvent(winner));
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
					entity.clearTag(Tag.IN_PLAY);
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
	 * Evaluate the {@link EntityRule} in the context of a particular event for the entity that
	 * this rule is attached to.
	 * 
	 * @param event
	 * @param rule
	 * @param entity
	 */
	void processRule(GameEvent event, EntityRule rule, GameEntity entity) {
		try {
			if(rule.isTriggered(event)) {
				// Let the rule access the event, game and entity objects
				scriptScope.put("event", event);
				scriptScope.put("rules", this);
				scriptScope.put("entity", entity);
				logger.info("Executing " + rule + " for " + event + " on " + entity);
				getScriptEngine().eval(rule.getScript(),scriptContext);
			}
		} catch (final ScriptException se) {
			logger.error("ScriptException processing rule: " + se.getMessage());
		} catch (Exception ex) {
			logger.error("Exception processing rule: " + ex.getMessage());
		}
	}
	
	/**
	 * Resets all entities' stats to their base values, then evaluates
	 * all buff rules to update them.
	 */
	void recalculateStats() {
		List<GameEntity> gameEntities = game.getEntities();
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
		// Finally, make sure no entity has a health greater than its max health
		// (this could happen as a result of losing a buff for example):
		for(GameEntity entity : gameEntities) {
			if(entity.getCurrentHealth() > entity.getMaxHealth()) {
				entity.setCurrentHealth(entity.getMaxHealth());
			}
		}
	}

}
