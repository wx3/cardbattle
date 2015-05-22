package com.wx3.cardbattle.game.messages;

import java.util.HashSet;
import java.util.Set;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;

/**
 * Like the GameView, creates a serializable representation of an entity relative
 * to a player, showing only information that player can see.  
 * 
 * @author Kevin
 *
 */
public class GameEntityView {
	public int id;
	public String name;
	public int cardId;
	public Set<String> tags;
	
	public static GameEntityView createViewForPlayer(GameEntity entity, GamePlayer player) {
		GameEntityView view = new GameEntityView();
		view.id = entity.getId();
		if(player.canSee(entity)) {
			view.name = entity.name;
			view.cardId = entity.getCreatingCard().getId();
			view.tags = new HashSet<String>(entity.getTags());
		}
		return view;
	}
}
