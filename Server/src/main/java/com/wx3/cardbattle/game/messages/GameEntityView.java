package com.wx3.cardbattle.game.messages;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.Tag;

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
	public String ownerName;
	/**
	 * If this entity is not visible to the player (e.g., in an opponent's hand)
	 * this will be false.
	 */
	public boolean visible;
	public int cardId;
	public Set<String> tags;
	public Map<String, Integer> stats;
	public Map<String, Integer> vars;
	
	public static GameEntityView createViewForPlayer(GameEntity entity, GamePlayer player) {
		GameEntityView view = new GameEntityView();
		view.id = entity.getId();
		view.ownerName = entity.getOwner().getUsername();
		if(player.canSee(entity)) {
			view.visible = true;
			view.name = entity.name;
			if(entity.getCreatingCard() != null) {
				view.cardId = entity.getCreatingCard().getId();
			}
			view.tags = new HashSet<String>(entity.getTags());
			view.stats = entity.getCurrentStats();
			view.vars = entity.getCurrentVars();
		} else {
			view.visible = false;
			// If an entity is in hand, that tag is visible:
			view.tags = new HashSet<String>();
			if(entity.hasTag(Tag.IN_HAND)) {
				view.tags.add(Tag.IN_HAND);
			}
		}
		return view;
	}
}
