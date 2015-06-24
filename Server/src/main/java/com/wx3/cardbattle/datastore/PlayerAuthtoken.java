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

import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import com.wx3.cardbattle.game.GameInstance;
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
	
	public PlayerAuthtoken() {}
	
	public PlayerAuthtoken(GamePlayer player, GameInstance game) {
		this.player = player;
		this.gameId = game.getId();
		this.authtoken = generateToken();
	}
	
	public PlayerAuthtoken(GamePlayer player, GameInstance game, String token) {
		this.player = player;
		this.gameId = game.getId();
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
