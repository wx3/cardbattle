package com.wx3.cardbattle.game.messages;

import java.util.ArrayList;
import java.util.List;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;

/**
 * Like the GameView, represents a player's view of a player, concealing information that
 * he should not be able to see.
 * 
 * @author Kevin
 *
 */
public class GamePlayerView {

	public String username;
	
	public int deckSize;
	public List<GameEntityView> inHand = new ArrayList<GameEntityView>();
	public List<GameEntityView> inPlay = new ArrayList<GameEntityView>();
	
	public static GamePlayerView createViewForPlayer(GamePlayer viewed, GamePlayer viewer) {
		GamePlayerView view = new GamePlayerView();
		view.username = viewed.getUsername();
		view.deckSize = viewed.getPlayerDeck().size();
		List<GameEntity> inhand = viewed.getPlayerHand();
		for(GameEntity e : inhand) {
			GameEntityView ev = GameEntityView.createViewForPlayer(e, viewer);
			view.inHand.add(ev);
		}
		return view;
	}
}
