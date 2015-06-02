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
	/**
	 * Which player is this the view for?
	 */
	public String playerName;
	public Map<String, GamePlayerView> players = new HashMap<String, GamePlayerView>();
	
	public static GameView createViewForPlayer(GameInstance game, GamePlayer player) {
		GameView view = new GameView();
		view.turn = game.getTurn();
		view.playerName = player.getUsername();
		for(GamePlayer p : game.getPlayers()) {
			GamePlayerView pv = GamePlayerView.createViewForPlayer(p, player);
			view.players.put(p.getUsername(), pv);
		}
		return view;
	}

	public GameView() {}
	
	
}
