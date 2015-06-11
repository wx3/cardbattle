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
package com.wx3.cardbattle.datastore;

import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.hibernate.Criteria;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.criterion.Restrictions;
import org.hibernate.service.ServiceRegistry;

import com.wx3.cardbattle.game.Card;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.game.gameevents.GameStartEvent;
import com.wx3.cardbattle.game.gameevents.KilledEvent;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;

/**
 * Handles persistence for the game, both long term via hibernate and 
 * through local memory.
 * 
 * @author Kevin
 *
 */
public class Datastore {

	private static SessionFactory sessionFactory;
	
	private Map<Long, GameInstance> gameInstances = new HashMap<Long, GameInstance>();
	
	public Datastore() {
		sessionFactory = createSessionFactory();
	}
	
	public void saveUser(User user) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(user);
    	session.getTransaction().commit();
	}
	
	public User getUser(String username) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(User.class);
    	User user = (User) criteria.add(Restrictions.eq("username",username)).uniqueResult();
    	session.getTransaction().commit();
		return user;
	}
	
	public Collection<Card> getCards() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	// If we're using some kind of SQL database, there may be a behind-the-scenes
    	// join creating multiple results, so we need the DISTINCT_ROOT_ENTITY transformer:
    	@SuppressWarnings("unchecked")
    	List<Card> cardList = session.createCriteria(Card.class)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
    		.list();
    	session.getTransaction().commit();
    	return cardList;
	}
	
	public void createValidator(PlayValidator pv) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(pv);
    	session.getTransaction().commit();
	}
	
	public void createRule(EntityRule rule) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(rule);
    	session.getTransaction().commit();
	}
	
	public Collection<EntityRule> getRules() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	@SuppressWarnings("unchecked")
		List<EntityRule> ruleList = session.createCriteria(EntityRule.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
				.list();
    	session.getTransaction().commit();
    	return ruleList;
	}
	
	/**
	 * Create a new game instance and the corresponding game players, persisting them
	 * to the database.
	 * @param game
	 * @param users
	 */
	public GameInstance createGame(List<User> users) {
		GameInstance game = new GameInstance();
		game.setRules(getRules());
		game.setCards(getCards());
		game.setGameRules(getGameRules());
		Session session = sessionFactory.openSession();
		SecureRandom random = new SecureRandom();
    	session.beginTransaction();
    	session.save(game);
    	session.getTransaction().commit();
    	session = sessionFactory.openSession();
    	session.beginTransaction();
    	int i = 0;
    	for(User user : users) {
        	String token =  new BigInteger(130, random).toString(32);
        	GamePlayer player = new GamePlayer(user, game.getId(), i);
        	PlayerAuthtoken auth = new PlayerAuthtoken(player, token);
        	session.save(auth);
        	List<Card> gameDeck = new ArrayList<Card>(user.getCurrentDeck());
        	player.setPlayerDeck(gameDeck);
    		session.save(player);
    		game.addPlayer(player);
    		++i;
    	}
    	session.getTransaction().commit();
		gameInstances.put(game.getId(), game);
		return game;
	}
	
	public List<PlayerAuthtoken> getAuthtokens(long gameId) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria cr = session.createCriteria(PlayerAuthtoken.class);
    	cr.add(Restrictions.eq("gameId", gameId));
    	@SuppressWarnings("unchecked")
    	List<PlayerAuthtoken> tokens = cr.list();
    	return tokens;
	}
	
	public GameInstance getGame(long id) {
		return gameInstances.get(id);
	}
	
	public void createCard(Card card) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(card);
    	session.getTransaction().commit();
	}
	
	/**
	 * Find the GamePlayer with the corresponding authentication token
	 * @param token
	 * @return
	 * @throws AuthenticationException 
	 */
	public GamePlayer authenticate(String token) throws AuthenticationException {
		if(token.isEmpty() || token == null) {
			throw new AuthenticationException(AuthenticationException.NO_TOKEN);
		}
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(PlayerAuthtoken.class);

    	PlayerAuthtoken authtoken = (PlayerAuthtoken) criteria.add(Restrictions.eq("authtoken", token)).uniqueResult();

    	session.getTransaction().commit();
    	if(authtoken == null) {
    		throw new AuthenticationException(AuthenticationException.BAD_TOKEN);
    	}
    	// Note that the GamePlayer we retrieve from the DB is not the one attached to the
    	// game, but has the same data, so we use its values to get the game id and player id
    	GamePlayer player = authtoken.getPlayer();
    	GameInstance game = getGame(player.getGameId());
    	if(game == null) {
    		throw new AuthenticationException(AuthenticationException.MISSING_GAME);
    	}
    	GamePlayer gamePlayer = game.getPlayer(player.getId());
    	if(gamePlayer == null) {
    		throw new AuthenticationException(AuthenticationException.UNKNOWN);
    	}
    	return gamePlayer;
	}
	
	private List<EntityRule> getGameRules() {
		List<EntityRule> rules = new ArrayList<EntityRule>();
		EntityRule gameOverRule = EntityRule.createRule(KilledEvent.class, "if(event.getEntity().hasTag('PLAYER')){rules.gameOver()}", "GAME_OVER", "Detects end of game on player death.");
		rules.add(gameOverRule);
		return rules;
	}
	
	private SessionFactory createSessionFactory() {
    	Configuration configuration = new Configuration().configure(); 
    	StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
        serviceRegistryBuilder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
}
