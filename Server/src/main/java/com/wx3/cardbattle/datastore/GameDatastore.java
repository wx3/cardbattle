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

package com.wx3.cardbattle.datastore;

import java.util.Collection;
import java.util.List;

import com.wx3.cardbattle.game.EntityPrototype;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;

/**
 * The Datastore stores persistent data that needs to be available outside of 
 * a game session, or across different game sessions. This includes "static"
 * game data like cards, rules, and validators, as well as information 
 * about game instances so a central gateway/load balancer can access it.
 * <p>
 * @author Kevin
 *
 */
public interface GameDatastore {

	/**
	 * Refresh the datastores local cache of static game data (cards, rules, etc.)
	 */
	public abstract void loadCache();

	public abstract void saveUser(User user);

	public abstract User getUser(String username);

	public abstract Collection<EntityPrototype> getCards();

	public abstract EntityPrototype getPrototype(String name);

	public abstract EntityPrototype getPrototype(int id);

	public abstract Collection<EntityRule> getRules();

	public abstract EntityRule getRule(String id);

	public abstract void createValidator(PlayValidator pv);

	public abstract void createRule(EntityRule rule);
	
	public abstract GameRecord newGameRecord();
	
	public abstract PlayerAuthtoken newAuthToken(GamePlayer player, GameRecord gameRecord); 
	
	public abstract List<PlayerAuthtoken> getAuthtokens(long gameId);
	
	public abstract void createPrototype(EntityPrototype card);

	/**
	 * Find the PlayerAuthtoken with the corresponding token string
	 * @param token
	 * @return
	 * @throws AuthenticationException 
	 */
	public abstract PlayerAuthtoken authenticate(String token);

}