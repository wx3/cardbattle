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
package com.wx3.cardbattle.server;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.GamePlayer;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

/**
 * Once a client has completed the handshake upgrading the HTTP connection to a 
 * websocket, this handler will accept JSON commands, such as a request to
 * join a specific game.
 * 
 * @author Kevin
 *
 */
public class WebsocketInitialHandler extends SimpleChannelInboundHandler<Object> {

	final Logger logger = LoggerFactory.getLogger(WebsocketInitialHandler.class);
	
	private WebSocketServerHandshaker handshaker;
	private NettyWebSocketServer server;
	
	public WebsocketInitialHandler(NettyWebSocketServer server, WebSocketServerHandshaker handshaker) {
		this.server =server;
		this.handshaker = handshaker;
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
	        //try {
	        	String text = ((TextWebSocketFrame) frame).text();
	            JsonParser parser = new JsonParser();
	     		JsonElement root = parser.parse(text);
	     		JsonObject obj = root.getAsJsonObject();
	     		handleJsonCommand(ctx, obj);
	        /*} catch(Exception ex) {
	        	logger.warn("Uncaught exception parsing WebSocketFrame: " + ex);
	        	ctx.close();
	        }*/
        } else {
        	logger.warn("Message was not WebSocketFrame.");
        	ctx.close();
        }
	}
	
	private void handleJsonCommand(ChannelHandlerContext ctx, JsonObject obj) {
		String commandName = obj.get("command").getAsString();
    	switch(commandName) {
	    	case "join":
	    		handleJoinGame(ctx, obj);
	    		break;
	    	case "testgame":
	    		handleTestGame(ctx, obj);
	    		break;
	    	default:
	    		logger.warn("Invalid command");
	    		ctx.close();
    	}
	}

	/**
     * Have the GameServer create a test game and return a list of player authtokens.
     * 
     * @param ctx
     * @param obj
     */
    private void handleTestGame(ChannelHandlerContext ctx, JsonObject obj) {
    	List<PlayerAuthtoken> authtokens = server.getGameServer().createTestGame();
    	Gson gson = new Gson();
    	Map<String, String> playerTokens = new HashMap<String,String>();
    	for(PlayerAuthtoken token : authtokens) {
    		playerTokens.put(token.getPlayer().getUsername(), token.getAuthtoken());
    	}
    	String resp = gson.toJson(playerTokens);
    	ctx.channel().writeAndFlush(new TextWebSocketFrame(resp));
    }
    
    /**
     * When a client tries to join a game, the message should include an authtoken,
     * which the gameserver will use to authenticate the client as a player in a 
     * particular game instance. If successful, this handler will be replaced by
     * a WebsocketGameHandler. 
     * 
     * @param ctx
     * @param obj
     */
    private void handleJoinGame(ChannelHandlerContext ctx, JsonObject obj) {
    	try {
    		String token = obj.get("authtoken").getAsString();
        	GamePlayer player = server.getGameServer().authenticate(token);
        	WebsocketMessageHandler messageHandler = new WebsocketMessageHandler(ctx.channel());
        	player.connect(messageHandler);
        	ctx.channel().pipeline().replace(WebsocketInitialHandler.class, "MessageHandler", new WebsocketGameHandler(server.getGameServer(), player));
        } catch (AuthenticationException authex) {
        	logger.info("Authentication exception, closing");
        	ctx.channel().writeAndFlush(new TextWebSocketFrame("Authentication Error: " + authex.getCode()));
        	ctx.channel().close();
        } /*catch (Exception ex) {
        	logger.warn("Unexpected exception in auth: " + ex.getMessage());
        	ctx.channel().close();
        }*/
    }
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.warn("Exception caught: " + cause.getStackTrace());
        ctx.close();
    }
    
}
