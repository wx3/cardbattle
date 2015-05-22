package com.wx3.cardbattle.netty;

import com.wx3.cardbattle.GameServer;
import com.wx3.cardbattle.datastore.Datastore;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;

public class NettyWebSocketServer {
	
	private GameServer gameserver;
	private int port;
	
	public NettyWebSocketServer(GameServer gameserver, int port) 
	{
		this.gameserver = gameserver;
		this.port = port;
	}

	public void start() {
		EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup)
             .channel(NioServerSocketChannel.class)
             .handler(new LoggingHandler(LogLevel.INFO))
             .childHandler(new WebSocketServerInitializer(this, null));
            
            Channel ch = b.bind(port).sync().channel();
            // This blocks until the channel is closed:
            ch.closeFuture().sync();

        } catch (InterruptedException e) {
			// What do we do with an interupted exception?
			e.printStackTrace();
		} finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
	}
	
	public GameServer getGameServer() {
		return gameserver;
	}
}
