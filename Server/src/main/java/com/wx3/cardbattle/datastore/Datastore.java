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
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	session.save(user);
    	session.getTransaction().commit();
	}
	
	public User getUser(String username) {
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(User.class);
    	User user = (User) criteria.add(Restrictions.eq("username",username)).uniqueResult();
    	session.getTransaction().commit();
		return user;
	}
	
	public Collection<Card> getCards() {
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	@SuppressWarnings("unchecked")
    	List<Card> cardList = session.createCriteria(Card.class).list();
    	session.getTransaction().commit();
    	return cardList;
	}
	
	public void createValidator(PlayValidator pv) {
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	session.save(pv);
    	session.getTransaction().commit();
	}
	
	public void createRule(EntityRule rule) {
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	session.save(rule);
    	session.getTransaction().commit();
	}
	
	public Collection<EntityRule> getRules() {
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	@SuppressWarnings("unchecked")
		List<EntityRule> ruleList = session.createCriteria(EntityRule.class).list();
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
		Session session = sessionFactory.getCurrentSession();
		SecureRandom random = new SecureRandom();
    	session.beginTransaction();
    	session.save(game);
    	session.getTransaction().commit();
    	session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	int i = 0;
    	for(User user : users) {
        	String token =  new BigInteger(130, random).toString(32);
        	
        	// For testing, the token is the username:
        	token = user.getUsername();
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
	
	public GameInstance getGame(long id) {
		return gameInstances.get(id);
	}
	
	public void createCard(Card card) {
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	session.save(card);
    	session.getTransaction().commit();
	}
	
	/**
	 * Find the GamePlayer with the corresponding authentication token
	 * @param token
	 * @return
	 */
	public GamePlayer authenticate(String token) {
		Session session = sessionFactory.getCurrentSession();
    	session.beginTransaction();
    	Criteria criteria = session.createCriteria(PlayerAuthtoken.class);

    	PlayerAuthtoken authtoken = (PlayerAuthtoken) criteria.add(Restrictions.eq("authtoken", token)).uniqueResult();

    	session.getTransaction().commit();
    	if(authtoken == null) {
    		throw new RuntimeException("Unable to authenticate player with token");
    	}
    	// Note that the GamePlayer we retrieve from the DB is not the one attached to the
    	// game, but has the same data, so we use its values to get the game id and player id
    	GamePlayer player = authtoken.getPlayer();
    	GameInstance game = getGame(player.getGameId());
    	if(game == null) {
    		throw new RuntimeException("Player authenticated, but game instance '" + player.getGameId() + "' not found on this server");
    	}
    	GamePlayer gamePlayer = game.getPlayer(player.getId());
    	if(gamePlayer == null) {
    		throw new RuntimeException("Player somehow not found in appropriate game instance.");
    	}
    	return gamePlayer;
	}
	
	private SessionFactory createSessionFactory() {
    	Configuration configuration = new Configuration().configure(); 
    	StandardServiceRegistryBuilder serviceRegistryBuilder = new StandardServiceRegistryBuilder();
        serviceRegistryBuilder.applySettings(configuration.getProperties());
        ServiceRegistry serviceRegistry = serviceRegistryBuilder.build();
        return configuration.buildSessionFactory(serviceRegistry);
    }
}
