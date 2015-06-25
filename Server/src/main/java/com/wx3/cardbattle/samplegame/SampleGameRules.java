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
package com.wx3.cardbattle.samplegame;

import java.util.ArrayList;
import java.util.List;

import com.wx3.cardbattle.game.EntityStats;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.RuleException;
import com.wx3.cardbattle.game.RuleSystem;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.Tag;
import com.wx3.cardbattle.game.gameevents.KilledEvent;
import com.wx3.cardbattle.game.gameevents.StartTurnEvent;
import com.wx3.cardbattle.game.rules.EntityRule;

/**
 * @author Kevin
 *
 */
public class SampleGameRules extends RuleSystem {

	/**
	 * @param game
	 */
	SampleGameRules(GameInstance game) {
		super(game);
		addGlobalRules();
	}
	
	void addGlobalRules() {
		List<EntityRule> rules = new ArrayList<EntityRule>();
		EntityRule gameOverRule = EntityRule.createRule(KilledEvent.class, "if(event.getEntity().hasTag('PLAYER')){rules.gameOver()}", "GAME_OVER", "Detects end of game on player death.");
		rules.add(gameOverRule);
		setGlobalRules(rules);
	}
	
	@Override
	public void addPlayer(GamePlayer player) {
		GameEntity playerEntity = spawnEntity();
		playerEntity.name = player.getUsername();
		playerEntity.setTag(Tag.PLAYER);
		playerEntity.setTag(Tag.IN_PLAY);
		
		playerEntity.setBaseStat(EntityStats.MAX_HEALTH, 100);
		playerEntity.setVar(GameEntity.CURRENT_HEALTH, playerEntity.getStat(EntityStats.MAX_HEALTH));
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

}
