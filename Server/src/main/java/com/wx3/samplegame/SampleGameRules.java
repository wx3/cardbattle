/**
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
 * 
 */
/**
 * 
 */
package com.wx3.samplegame;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import javax.script.ScriptException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.game.EntityPrototype;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.RuleException;
import com.wx3.cardbattle.game.RuleSystem;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.game.gameevents.StartTurnEvent;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;
import com.wx3.samplegame.commands.PlayCardCommand;
import com.wx3.samplegame.events.DamageEvent;
import com.wx3.samplegame.events.DrawCardEvent;
import com.wx3.samplegame.events.GameOverEvent;
import com.wx3.samplegame.events.KilledEvent;
import com.wx3.samplegame.events.PlayCardEvent;
import com.wx3.samplegame.events.SummonMinionEvent;

/**
 * @author Kevin
 *
 */
public class SampleGameRules extends RuleSystem<SampleEntity> {
	
	/**
	 * @param game
	 */
	public SampleGameRules(GameInstance<SampleEntity> game) {
		super(game);
	}

	private transient final Logger logger = LoggerFactory.getLogger(SampleGameRules.class);
	
	// Our stats:
	public static final String COST = "COST";
	public static final String MAX_HEALTH = "MAX_HEALTH";
	public static final String ATTACK = "ATTACK";
	public static final String ATTACKS_PER_TURN = "ATTACKS_PER_TURN";
	
	// Applied to Minions:
	public static final String MINION = "MINION";
	// Applied to Spells:
	public static final String SPELL = "SPELL";
	// This entity is a card in hand:
	public static final String IN_HAND = "IN_HAND";
	
	// Our vars:
	public static final String CURRENT_HEALTH = "CURRENT_HEALTH";
	public static final String ATTACKS_REMAINING = "ATTACKS_REMAINING";
	
	void addGlobalRules() {
		List<EntityRule> rules = new ArrayList<EntityRule>();
		EntityRule gameOverRule = EntityRule.createRule(KilledEvent.class, "if(event.getEntity().hasTag('PLAYER')){rules.gameOver()}", "GAME_OVER", "Detects end of game on player death.");
		rules.add(gameOverRule);
		setGlobalRules(rules);
	}
	
	@Override
	protected SampleEntity createEntityInstance() {
		return new SampleEntity();
	}
	
	@Override
	protected void startTurn() {
		super.startTurn();
		// At the start of every turn, set an entity's attacks remaining = max attacks.
		for(SampleEntity entity : game.getEntities()) {
			entity.resetAttacks();
		}
	}
	
	public List<SampleEntity> getPlayerHand(GamePlayer player) {
		List<SampleEntity> hand = game.getEntities().stream().filter(
				e -> e.getOwner() == player && e.hasTag(IN_HAND)
				).collect(Collectors.toList());
		return hand;
	}
	
	/**
	 * Find the player's entity.
	 * 
	 * @param player
	 * @return
	 */
	public SampleEntity getPlayerEntity(GamePlayer player) {
		// There should be at most one:
		return game.getEntities().stream().filter(
				e -> e.getOwner() == player && e.hasTag(SampleGameRules.PLAYER)).findFirst().orElse(null);
	}
	
	
	@Override
	public void addPlayer(GamePlayer player) {
		GameEntity playerEntity = spawnEntity();
		playerEntity.name = player.getUsername();
		playerEntity.setTag(SampleGameRules.PLAYER);
		playerEntity.setTag(SampleGameRules.IN_PLAY);
		
		playerEntity.setBaseStat(MAX_HEALTH, 100);
		playerEntity.setVar(CURRENT_HEALTH, playerEntity.getStat(MAX_HEALTH));
		playerEntity.setOwner(player);
		// Eventually player rules should move out of here into the database/bootstrap:
		String s2 = "if(entity.getOwner() == rules.getCurrentPlayer(event.getTurn())) {"
				+ "if(event.getTurn() < 2){"
				+ "rules.drawCard(entity.getOwner());"
				+ "rules.drawCard(entity.getOwner());"
				+ "rules.drawCard(entity.getOwner());"
				+ "rules.trace('Initial draw for ' + entity.getOwner());"
				+ "} else {"
				+ "rules.drawCard(entity.getOwner());"
				+ "}"
				+ "}";
		EntityRule drawRule = EntityRule.createRule(StartTurnEvent.class, s2, "PLAYER_DRAW", "Player draws at start of turn.");
		addRule(playerEntity, drawRule, null);
		
		player.setEntity(playerEntity);
	};
	
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
		EntityPrototype card = player.drawCard();
		if(card != null) {
			GameEntity entity = instantiatePrototype(card);
			entity.setTag(IN_HAND);
			entity.setOwner(player);
			addEvent(new DrawCardEvent(player, entity, cause));
			return entity;
		}
		else {
			logger.info("Hand is empty.");
			return null;
		}
	}
	
	
	/**
	 * Play a card onto the board with an optional targetEntity
	 * 
	 * @param cardEntity
	 */
	public void playCard(SampleEntity cardEntity, SampleEntity targetEntity) {
		String msg = "Playing " + cardEntity;
		if(targetEntity != null) msg += " on " + targetEntity;
		logger.info(msg);
		cardEntity.setTag(SampleGameRules.IN_PLAY);
		cardEntity.clearTag(IN_HAND);
		PlayCardEvent event = new PlayCardEvent(cardEntity, targetEntity);
		addEvent(event);
		if(cardEntity.isMinion()) {
			addEvent(new SummonMinionEvent(cardEntity));
		}
		else {
			removeEntity(cardEntity);
		}
	}
	
	
	/**
	 * Deliver damage from attacker to the target equal to its attack stat,
	 * and vice versa.
	 * 
	 * @param attacker
	 * @param target
	 */
	public void attack(SampleEntity attacker, SampleEntity target) {
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
		int attackerAttack = attacker.getStat(ATTACK);
		int targetAttack = target.getStat(ATTACK);
		if(attackerAttack <= 0) {
			throw new RuleException("Attacker has no attack value");
		}
		damageEntity(target, attackerAttack, attacker);
		damageEntity(attacker, targetAttack, target);
		attacker.setAttacksRemaining(attacker.getAttacksRemaining() - 1);
	}
	
	/**
	 * Heal the entity by amount up to the entity's max health.
	 * 
	 * @param entity
	 * @param amount
	 */
	public void healEntity(SampleEntity entity, int amount) {
		entity.setCurrentHealth(entity.getCurrentHealth() + amount);
		if(entity.getCurrentHealth() > entity.getMaxHealth()) {
			entity.setCurrentHealth(entity.getMaxHealth());
		}
	}
	
	/**
	 * Heal the entity by however much damage it's taken.
	 * @param entity
	 */
	public void healEntity(SampleEntity entity) {
		int damage = entity.getMaxHealth() - entity.getCurrentHealth();
		healEntity(entity, damage);
	}

	/**
	 * Deliver damage to an entity, destroying it if total
	 * damage exceeds its max health.
	 * 
	 * @param entity
	 * @param damage
	 */
	public void damageEntity(SampleEntity entity, int damage, GameEntity cause) {
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
	
	@Override
	public void gameOver() {
		super.gameOver();
		GamePlayer winner = null;
		for(GamePlayer player : game.getPlayers()) {
			SampleEntity entity = getPlayerEntity(player);
			if(entity != null && entity.getCurrentHealth() > 0) {
				winner = player;
			}
		}
		GameOverEvent event = new GameOverEvent(winner);
		addEvent(event);
	}
	
	/**
	 * Test whether the PlayCardCommand is valid. 
	 * 
	 * @param result
	 * @param command
	 */
	public void validatePlayCard(ValidationResult result, PlayCardCommand command) {
		// If the command's card has no validator, we don't need to do anything
		if(command.getCard().getValidator() == null) return;
		try {
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
	
	@Override
	protected void recalculateStats() {
		// TODO Auto-generated method stub
		super.recalculateStats();
		// Finally, make sure no entity has a health greater than its max health
		// (this could happen as a result of losing a buff for example):
		for(SampleEntity entity : game.getEntities()) {
			if(entity.getCurrentHealth() > entity.getMaxHealth()) {
				entity.setCurrentHealth(entity.getMaxHealth());
			}
		}
	}

}
