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

package com.wx3.cardbattle;

import com.wx3.cardbattle.datastore.GameDatastore;
import com.wx3.cardbattle.datastore.HibernateDatastore;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.UpdateGamesTask;
import com.wx3.cardbattle.server.GameServer;
import com.wx3.cardbattle.server.NettyWebSocketServer;

/**
 * The main entry point for the game application, creates a Datastore,
 * GameServer and Netty server. 
 *
 */
public class CardBattle 
{
	static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
    
    public static void main( String[] args )
    {
    	GameDatastore datastore = new HibernateDatastore();
    	bootstrap(datastore);
    	
    	GameServer gameserver = new GameServer(datastore);
    	gameserver.start();
    	
    	NettyWebSocketServer nettyServer = new NettyWebSocketServer(gameserver, PORT);
    	nettyServer.start();
    	
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
