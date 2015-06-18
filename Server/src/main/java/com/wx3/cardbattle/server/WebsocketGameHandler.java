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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.GameServer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.JsonCommandFactory;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

/**
 * Parses incoming messages as {@link GameCommand}s and sends them to the player 
 * object.
 * 
 * @author Kevin
 *
 */
public class WebsocketGameHandler extends
		SimpleChannelInboundHandler<Object>  {
	
	final Logger logger = LoggerFactory.getLogger(WebsocketGameHandler.class);
	
	private GamePlayer player;
	private GameServer gameServer;
	private JsonCommandFactory commandFactory;
	
	public WebsocketGameHandler(GameServer gameServer, GamePlayer player) {
		this.gameServer = gameServer;
		this.player = player;
		this.commandFactory = new JsonCommandFactory();
	}
	
	private void close() {
		if(player != null) player.disconnect();
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
	{
		WebSocketFrame frame = (WebSocketFrame) msg;
		if (frame instanceof CloseWebSocketFrame) {
			System.err.printf("%s closed websocket", ctx.channel());
            close();
            return;
        }
		if (frame instanceof TextWebSocketFrame) {
			//try {
				String request = ((TextWebSocketFrame) frame).text();
				JsonParser parser = new JsonParser();
				JsonElement root = parser.parse(request);
				JsonObject obj = root.getAsJsonObject();
				String commandName = obj.get("command").getAsString();
				GameCommand command = commandFactory.createCommand(commandName, obj.get("object"));
				logger.info("Received command: " + command.toString());
				player.handleCommand(command);
			/*} catch (Exception ex) {
				logger.error("Failed to process command.", ex);
			}*/
			
		} else {
			throw new RuntimeException("Invalid websocket frame");
		}
	}
/*
	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
		logger.warn("Exception caught, kicking player: " + cause);
		close();
    }*/

}
