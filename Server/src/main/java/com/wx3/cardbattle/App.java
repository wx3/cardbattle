package com.wx3.cardbattle;

import java.io.IOException;
import java.net.URL;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.service.ServiceRegistry;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.wx3.cardbattle.datastore.Datastore;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.User;
import com.wx3.cardbattle.netty.NettyWebSocketServer;

/**
 * The main entry point for the game application
 *
 */
public class App 
{
	static final boolean SSL = System.getProperty("ssl") != null;
    static final int PORT = Integer.parseInt(System.getProperty("port", SSL? "8443" : "8080"));
    
    public static void main( String[] args )
    {
    	Datastore datastore = new Datastore();
    	
    	GameServer gameserver = new GameServer(datastore);
    	
    	TestSetup test = new TestSetup(datastore);
    	GameInstance game = test.setup();
    	game.start();
    	
    	NettyWebSocketServer nettyServer = new NettyWebSocketServer(gameserver, PORT);
    	nettyServer.start();
    	
    }
    
}
