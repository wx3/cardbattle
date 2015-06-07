package com.wx3.cardbattle.game.messages;

import java.util.ArrayList;
import java.util.List;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;

/**
 * Like the GameView, represents a player's view of a player.
 * 
 * @author Kevin
 *
 */
public class GamePlayerView {

	public String username;
	public long id;
	public int position;
	public int deckSize;

	
	public static GamePlayerView createViewForPlayer(GamePlayer viewed, GamePlayer viewer) {
		GamePlayerView view = new GamePlayerView();
		view.username = viewed.getUsername();
		view.id = viewed.getId();
		view.position = viewed.getPosition();
		view.deckSize = viewed.getPlayerDeck().size();
		return view;
	}
}
