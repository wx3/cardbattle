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
package com.wx3.cardbattle;

import java.util.List;

import com.wx3.cardbattle.ai.AIManager;
import com.wx3.cardbattle.ai.SimpleAI;
import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.datastore.HibernateDatastore;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.GameServer;
import com.wx3.samplegame.Bootstrap;
import com.wx3.samplegame.SampleGameCommandFactory;
import com.wx3.samplegame.SampleGameServer;

/**
 * @author Kevin
 *
 */
public class AITest {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		GameDatastore datastore = new HibernateDatastore();
    	bootstrap(datastore);
    	SampleGameCommandFactory gameFactory = new SampleGameCommandFactory(datastore);
    	GameServer gameserver = new SampleGameServer(datastore, gameFactory);
    	gameserver.start();
    	
    	AIManager aimanager = new AIManager(1);
    	
    	aimanager.start();
    	
    	for(int i = 0; i < 100; i++) {
	    	List<PlayerAuthtoken> tokens = gameserver.createTestGame();
	    	for(PlayerAuthtoken token : tokens) {
	    		try {
					GamePlayer player = gameserver.authenticate(token.getAuthtoken());
					SimpleAI ai = new SimpleAI(player);
					player.connect(ai);
					aimanager.registerAI(ai);
				} catch (AuthenticationException e) {
					// This shouldn't happen since we're authenticating with the token
					// just supplied.
					e.printStackTrace();
				}
	    	}
    	}
	}
	
	/**
     * Load initial game data into the datastore
     * @param datastore
     */
    private static void bootstrap(GameDatastore datastore) {
    	Bootstrap test = new Bootstrap(datastore);
    	test.importData("csv");
    	datastore.loadCache();
    }

}
