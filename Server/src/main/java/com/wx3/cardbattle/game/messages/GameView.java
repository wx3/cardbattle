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
package com.wx3.cardbattle.game.messages;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;

/**
 * Represents a player's view of the current game state in a way
 * that can be serialized via JSON, hiding information that the player
 * should not be able to see, such as cards in the opponent's hand.
 * 
 * @author Kevin
 *
 */
public class GameView {
	
	public int turn;
	public String currentPlayer;
	/**
	 * Which player is this the view for?
	 */
	public String playerName;
	public Map<String, GamePlayerView> players = new HashMap<String, GamePlayerView>();
	
	public List<GameEntityView> entities = new ArrayList<GameEntityView>();
	
	public static GameView createViewForPlayer(GameInstance game, GamePlayer player) {
		GameView view = new GameView();
		view.currentPlayer = game.getRuleEngine().getCurrentPlayer().getUsername();
		view.playerName = player.getUsername();
		for(GamePlayer p : game.getPlayers()) {
			GamePlayerView pv = GamePlayerView.createViewForPlayer(p, player);
			view.players.put(p.getUsername(), pv);
		}
		for(GameEntity entity : game.getEntities()) {
			view.entities.add(GameEntityView.createViewForPlayer(entity, player));
		}
		return view;
	}
	
	
}
