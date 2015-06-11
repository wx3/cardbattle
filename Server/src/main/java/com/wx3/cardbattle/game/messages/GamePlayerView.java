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
