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

import java.util.HashSet;
import java.util.Map;
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
public final class GameEntityView {
	
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
		if(entity.getOwner() != null) {
			view.ownerName = entity.getOwner().getUsername();
		}
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
			view.tags = new HashSet<String>();
		}
		return view;
	}
}
