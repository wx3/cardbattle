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
package com.wx3.cardbattle.networking;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.ValidationResult;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;
import com.wx3.cardbattle.game.messages.GameEntityJsonSerializer;
import com.wx3.cardbattle.server.GameServer;
import com.wx3.cardbattle.server.MessageHandler;
import com.wx3.cardbattle.server.OutboundMessage;

/**
 * Once a client has completed the handshake upgrading the HTTP connection to a 
 * websocket, this handler will accept JSON commands. Until a player is 
 * authenticated, these commands will be interpretted as either a Join request 
 * or passed along to the {@link GameServer}.
 * <p>
 * Once a player is authenticated, they will be translated into
 * {@link GameCommand}s by the GameServer.
 * 
 * @author Kevin
 *
 */
public class WebsocketHandler extends SimpleChannelInboundHandler<Object> implements MessageHandler {

	final Logger logger = LoggerFactory.getLogger(WebsocketHandler.class);
	
	private Channel channel;
	private GameServer gameServer;
	private GamePlayer player;
	
	public WebsocketHandler(Channel channel, GameServer gameServer) {
		this.gameServer = gameServer;
		this.channel = channel;
	}
	
	private boolean isAuthenticated() {
		if(player != null) return true;
		return false;
	}
	
	@Override
	public void disconnect() {
		if(player != null && player.isConnected()) {
			player.disconnect();
		}
		channel.close();
		
	}

	@Override
	public void handleMessage(OutboundMessage message) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(GameEntity.class, new GameEntityJsonSerializer());
		Gson gson = builder.create();
		String encoded = gson.toJson(message);
		this.channel.writeAndFlush(new TextWebSocketFrame(encoded));
		
	}
	
	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		if (msg instanceof WebSocketFrame) {
			WebSocketFrame frame = (WebSocketFrame) msg;
	        if (frame instanceof CloseWebSocketFrame) {
	        	ctx.close();
	            return;
	        }
	        if (frame instanceof PingWebSocketFrame) {
	            ctx.channel().write(new PongWebSocketFrame(frame.content().retain()));
	            return;
	        }
	        if (!(frame instanceof TextWebSocketFrame)) {
	            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass()
	                    .getName()));
	        }
	        String text = ((TextWebSocketFrame) frame).text();
	        JsonParser parser = new JsonParser();
     		JsonElement root = parser.parse(text);
     		JsonObject json = root.getAsJsonObject();
	        if(!isAuthenticated()) {
	        	String commandName = json.get("command").getAsString();
	        	if(commandName.equals("join")) {
	        		handleJoin(json);
	        	} 
	        	else {
	        		gameServer.handleJsonCommand(json, this);	
	        	}
	        } else {
	        	GameCommand<?> command = gameServer.createCommand(player, json);
	        	ValidationResult result = player.handleCommand(command);
				CommandResponseMessage message = new CommandResponseMessage(command, result);
				player.sendMessage(message);
	        }
        } else {
        	logger.warn("Message was not WebSocketFrame.");
        	ctx.close();
        }
	}
    
	private void handleJoin(JsonObject json) {
		if(!json.has("authtoken")) {
			throw new RuntimeException("No authtoken found in join JSON");
		}
		String token = json.get("authtoken").getAsString();
		try {
			player = gameServer.authenticate(token);
			player.connect(this);
		} catch (AuthenticationException e) {
			logger.warn("Authentication failure: " + e.getCode());
			disconnect();
		}
	}
    
    /*
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Exception caught: " + cause.getStackTrace());
        ctx.close();
    }
    */
    
}
