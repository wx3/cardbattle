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

import com.wx3.cardbattle.game.EntityPrototype;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.game.rules.EntityRule;
import com.wx3.cardbattle.game.rules.PlayValidator;

/**
 * This implementation handles long-term storage via Hibernate, so 
 * it can use memory for local development/testing or a SQL database such as MySQL
 * for staging/production.
 * <p>
 * See the hibernate.cfg.xml in resources for Hibernate configuration.
 * 
 * @author Kevin
 *
 */
public class HibernateDatastore implements GameDatastore {

	private static SessionFactory sessionFactory;
	
	// Cards & rules are only expected to change during game updates, so we should 
	// only need to load them once:
	private Map<Integer, EntityPrototype> cardsById = new HashMap<Integer, EntityPrototype>();
	private Map<String, EntityPrototype> cardsByName = new HashMap<String, EntityPrototype>();
	private Map<String, EntityRule> rulesById = new HashMap<String, EntityRule>();
	
	public HibernateDatastore() {
		sessionFactory = createSessionFactory();
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#loadCache()
	 */
	@Override
	public void loadCache() {
		Collection<EntityPrototype> cards = loadCards();
		for(EntityPrototype card : cards) {
			cardsById.put(card.getId(), card);
			cardsByName.put(card.getName(), card);
		}
		Collection<EntityRule> rules = loadRules();
		for(EntityRule rule : rules) {
			rulesById.put(rule.getId(), rule);
		}
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#saveUser(com.wx3.cardbattle.game.User)
	 */
	@Override
	public void saveUser(User user) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(user);
    	session.getTransaction().commit();
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#getUser(java.lang.String)
	 */
	@Override
	public User getUser(String username) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(User.class);
    	User user = (User) criteria.add(Restrictions.eq("username",username)).uniqueResult();
    	session.getTransaction().commit();
		return user;
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#getCards()
	 */
	@Override
	public Collection<EntityPrototype> getCards() {
		return cardsById.values();
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#getCard(java.lang.String)
	 */
	@Override
	public EntityPrototype getPrototype(String name) {
		if(!cardsByName.containsKey(name)) {
			throw new RuntimeException("Could not find card named '" + name + "' in datastore cache.");
		}
		return cardsByName.get(name);
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#getCard(int)
	 */
	@Override
	public EntityPrototype getPrototype(int id) {
		if(!cardsById.containsKey(id)) {
			throw new RuntimeException("Could not find card with id '" + id + "' in datastore cache");
		}
		return cardsById.get(id);
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#getRules()
	 */
	@Override
	public Collection<EntityRule> getRules() {
		return rulesById.values();
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#getRule(java.lang.String)
	 */
	@Override
	public EntityRule getRule(String id) {
		if(!rulesById.containsKey(id)) {
			throw new RuntimeException("Could not find rule with id '" + id + "' in datastore cache");
		}
		return rulesById.get(id);
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#createValidator(com.wx3.cardbattle.game.rules.PlayValidator)
	 */
	@Override
	public void createValidator(PlayValidator pv) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(pv);
    	session.getTransaction().commit();
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#createRule(com.wx3.cardbattle.game.rules.EntityRule)
	 */
	@Override
	public void createRule(EntityRule rule) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(rule);
    	session.getTransaction().commit();
	}
	
	public GameRecord newGameRecord() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	GameRecord gameRecord = new GameRecord();
    	session.save(gameRecord);
    	session.getTransaction().commit();
    	return gameRecord;
	}
	
	public PlayerAuthtoken newAuthToken(GamePlayer player, GameRecord gameRecord) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(player);
    	PlayerAuthtoken authtoken = new PlayerAuthtoken(player, gameRecord.getGameId()); 
    	session.save(authtoken);
    	session.getTransaction().commit();
    	return authtoken;
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#getAuthtokens(long)
	 */
	@Override
	public List<PlayerAuthtoken> getAuthtokens(long gameId) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria cr = session.createCriteria(PlayerAuthtoken.class);
    	cr.add(Restrictions.eq("gameId", gameId));
    	@SuppressWarnings("unchecked")
    	List<PlayerAuthtoken> tokens = cr.list();
    	return tokens;
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#createCard(com.wx3.cardbattle.game.Card)
	 */
	@Override
	public void createPrototype(EntityPrototype card) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	session.save(card);
    	session.getTransaction().commit();
	}
	
	/* (non-Javadoc)
	 * @see com.wx3.cardbattle.datastore.Datastore#authenticate(java.lang.String)
	 */
	@Override
	public PlayerAuthtoken authenticate(String token) {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(PlayerAuthtoken.class);
    	PlayerAuthtoken authtoken = (PlayerAuthtoken) criteria.add(Restrictions.eq("authtoken", token)).uniqueResult();
    	session.getTransaction().commit();
    	return authtoken;
	}
	
	private Collection<EntityPrototype> loadCards() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	// If we're using some kind of SQL database, there may be a behind-the-scenes
    	// join creating multiple results, so we need the DISTINCT_ROOT_ENTITY transformer:
    	@SuppressWarnings("unchecked")
    	List<EntityPrototype> cardList = session.createCriteria(EntityPrototype.class)
			.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
    		.list();
    	session.getTransaction().commit();
    	return cardList;
	}
	
	private Collection<EntityRule> loadRules() {
		Session session = sessionFactory.openSession();
    	session.beginTransaction();
    	@SuppressWarnings("unchecked")
		List<EntityRule> ruleList = session.createCriteria(EntityRule.class)
				.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY)  
				.list();
    	session.getTransaction().commit();
    	return ruleList;
	}
	
	private SessionFactory createSessionFactory() {
    	Configuration configuration = new Configuration().configure("hibernate.cfg.xml"); 
    	StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
        serviceRegistryBuilder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }

}
