package com.wx3.cardbattle.game;

import java.util.Queue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.wx3.cardbattle.game.gameevents.DamageEvent;
import com.wx3.cardbattle.game.gameevents.DrawCardEvent;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.gameevents.KilledEvent;
import com.wx3.cardbattle.game.rules.EntityRule;

/**
 * The GameRuleProcessor uses the Nashorn javascript engine to process
 * entity rules. The engine is created with a restrictive filter
 * to prevent rules from calling any Java objects/classes except
 * those supplied by the processor.
 * <p>
 * The rule processor provides common utility methods for accessing 
 * game logic from scripts. 
 *  
 * @author Kevin
 *
 */
public class GameRuleProcessor {
	
	final Logger logger = LoggerFactory.getLogger(GameRuleProcessor.class);

	private GameInstance game;
	private ScriptEngine engine;
	
	class RestrictiveFilter implements ClassFilter {

		@Override
		public boolean exposeToScripts(String s) {
			return false;
		}
		
	}
	
	GameRuleProcessor(GameInstance game) {
		this.game = game;
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory(); 
		
		this.engine = factory.getScriptEngine(new RestrictiveFilter());
		if(this.engine == null) {
			throw new RuntimeException("Unable to get script engine");
		}
	}
	
	/**
	 * Process any starting rules on the game
	 */
	void startup() {
		try {
			String script = game.getStartupScript();
			if(!Strings.isNullOrEmpty(script)) {
				engine.put("game", game);
				engine.eval(game.getStartupScript());
			}
		} catch (final ScriptException se) {
			logger.error("Exception processing startup script: " + se.getMessage());
			throw(new RuntimeException(se));
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
				engine.put("event", event);
				engine.put("rules", this);
				engine.put("entity", entity);
				logger.info("Executing " + rule + " for " + event + " on " + entity);
				engine.eval(rule.getScript());
			}
		} catch (final ScriptException se) {
			logger.error("Exception processing rule: " + se.getMessage());
		}
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
	
	public GameEntity drawCard() {
		return drawCard(getCurrentPlayer());
	}
	
	/**
	 * Deliver damage to an entity, destroying it if total
	 * damage exceeds its max health.
	 * 
	 * @param entity
	 * @param damage
	 */
	public void damageEntity(GameEntity entity, int damage) {
		// Cap damage at the entity's max health:
		if(damage > entity.getMaxHealth()) {
			damage = entity.getMaxHealth();
		}
		int currentDam = entity.getVar(GameEntity.DAMAGE_TAKEN);
		currentDam += damage;
		entity.setVar(GameEntity.DAMAGE_TAKEN, currentDam);
		game.addEvent(new DamageEvent(entity, damage));
		if(currentDam >= entity.getMaxHealth()) {
			killEntity(entity);
		}
	}
	
	/**
	 * Mark an entity for removal without firing a {@link KilledEvent}
	 * 
	 * @param entity
	 */
	public void removeEntity(GameEntity entity) {
		game.removeEntity(entity);
	}
	
	/**
	 * Mark an entity for removal and fire a {@link KilledEvent}
	 * @param entity
	 */
	public void killEntity(GameEntity entity) {
		KilledEvent event = new KilledEvent(entity);
		game.addEvent(event);
		game.removeEntity(entity);
	}
	
	/**
	 * Draw a card for the supplied player, creating the necessary
	 * card entity.
	 * 
	 * @param player
	 * @return The entity created by the draw
	 */
	public GameEntity drawCard(GamePlayer player) {
		Card card = player.drawCard();
		if(card != null) {
			GameEntity entity = instantiateCard(card);
			entity.setTag(Tag.IN_HAND);
			entity.setOwner(player);
			game.addEvent(new DrawCardEvent(player, entity));
			return entity;
		}
		else {
			return null;
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

}
