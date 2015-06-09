package com.wx3.cardbattle.game;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Queue;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import jdk.nashorn.api.scripting.ClassFilter;
import jdk.nashorn.api.scripting.NashornScriptEngineFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Strings;
import com.wx3.cardbattle.game.commands.PlayCardCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.game.gameevents.BuffRecalc;
import com.wx3.cardbattle.game.gameevents.DamageEvent;
import com.wx3.cardbattle.game.gameevents.DrawCardEvent;
import com.wx3.cardbattle.game.gameevents.EnchantEvent;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.gameevents.KilledEvent;
import com.wx3.cardbattle.game.gameevents.PlayCardEvent;
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
	
	final Logger logger = LoggerFactory.getLogger(GameRuleEngine.class);

	private GameInstance game;
	private ScriptEngine scriptEngine;
	
	class RestrictiveFilter implements ClassFilter {

		@Override
		public boolean exposeToScripts(String s) {
			return false;
		}
		
	}
	
	GameRuleEngine(GameInstance game) {
		this.game = game;
		NashornScriptEngineFactory factory = new NashornScriptEngineFactory(); 
		
		this.scriptEngine = factory.getScriptEngine(new RestrictiveFilter());
		if(this.scriptEngine == null) {
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
				scriptEngine.put("game", game);
				scriptEngine.eval(game.getStartupScript());
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
				scriptEngine.put("event", event);
				scriptEngine.put("rules", this);
				scriptEngine.put("entity", entity);
				logger.info("Executing " + rule + " for " + event + " on " + entity);
				scriptEngine.eval(rule.getScript());
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
						scriptEngine.put("rules", this);
						scriptEngine.put("entity", entity);
						logger.info("Recalculating buff " + rule + " on " + entity);
						try {
							scriptEngine.eval(rule.getScript());	
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
	
	void validatePlay(ValidationResult result, PlayCardCommand command) {
		// If the command's card has no validator, we don't need to do anything
		if(command.getCard().getValidator() == null) return;
		try {
			scriptEngine.put("command", command);
			scriptEngine.put("target", command.getTarget());
			scriptEngine.put("rules", this);
			scriptEngine.put("error", null);
			PlayValidator validator = command.getCard().getValidator();
			scriptEngine.eval(validator.getScript());
			if(scriptEngine.get("error") != null) {
				result.addError(scriptEngine.get("error").toString());
			}
		} catch (final ScriptException se) {
			result.addError("Scripting exception: " + se.getMessage());
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
		game.addEvent(new EnchantEvent(entity, rule, cause));
	}
	
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
		game.addEvent(new DamageEvent(entity, damage, cause));
		if(currentHealth <= 0) {
			killEntity(entity);
		}
	}
	
	/**
	 * Mark an entity for removal without firing a {@link KilledEvent}
	 * 
	 * @param entity
	 */
	public void removeEntity(GameEntity entity) {
		entity.clearTag(Tag.IN_PLAY);
		game.removeEntity(entity);
	}
	
	/**
	 * Mark an entity for removal and fire a {@link KilledEvent}
	 * @param entity
	 */
	void killEntity(GameEntity entity) {
		KilledEvent event = new KilledEvent(entity);
		game.addEvent(event);
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
			game.addEvent(new DrawCardEvent(player, entity, cause));
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
