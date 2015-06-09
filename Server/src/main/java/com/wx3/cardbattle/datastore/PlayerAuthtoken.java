package com.wx3.cardbattle.datastore;

import java.math.BigInteger;
import java.security.SecureRandom;

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.wx3.cardbattle.game.GamePlayer;

/**
 * A PlayerAuthtoken is used to authenticate a client as a particular 
 * GamePlayer
 * 
 * @author Kevin
 *
 */
@Entity
@Table(name = "player_authtokens")
public class PlayerAuthtoken {

	@Id
	private String authtoken;
	
	@OneToOne
	private GamePlayer player;
	
	private long gameId;
	
	public static String generateToken() {
		SecureRandom random = new SecureRandom();
		return  new BigInteger(130, random).toString(32);
	}
	
	public PlayerAuthtoken() {
		
	}
	
	public PlayerAuthtoken(GamePlayer player) {
		this.player = player;
		this.gameId = player.getGameId();
		this.authtoken = generateToken();
	}
	
	public PlayerAuthtoken(GamePlayer player, String token) {
		this.player = player;
		this.gameId = player.getGameId();
		this.authtoken = token;
	}
	
	public String getAuthtoken() {
		return authtoken;
	}
	
	public void setAuthtoken(String authtoken) {
		this.authtoken = authtoken;
	}
	
	public GamePlayer getPlayer() {
		return player;
	}
	
	public void setPlayer(GamePlayer player) {
		this.player = player;
	}
	
	public long getGameId() {
		return gameId;
	}
	
}
