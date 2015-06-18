/**
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
 * 
 */
/**
 * 
 */
package com.wx3.cardbattle.game;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.TimerTask;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.wx3.cardbattle.datastore.GameDatastore;

/**
 * Tries to call the update method on all games in the datastore.
 * 
 * @author Kevin
 *
 */
public class UpdateGamesTask extends TimerTask {
	
	final Logger logger = LoggerFactory.getLogger(UpdateGamesTask.class);
	
	private static final int EXPIRATION = 10;
	
	private GameDatastore datastore;
	private int updates;

	public UpdateGamesTask(GameDatastore datastore) {
		this.datastore = datastore;
	}

	@Override
	public void run() {
		Collection<GameInstance> games = datastore.getGames();
		for(GameInstance game : games) {
			try {
				if(game.isStopped()) {
					datastore.removeGame(game.getId());	
				} else {
					game.update();
				}
			} catch (Exception ex) {
				logger.error("Exception trying to update '" + game + "': " + ex.getMessage());
				if(game != null) {
					datastore.removeGame(game.getId());
				}
			}
		}
		if(updates % 100 == 0) {
			logger.info("Update #" + updates + " updated " + games.size() + " games");
		}
		++updates;
	}

}
