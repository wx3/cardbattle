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

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.gameevents.GameEvent;
import com.wx3.cardbattle.game.gameevents.GameOverEvent;
import com.wx3.cardbattle.game.messages.EventMessage;
import com.wx3.cardbattle.game.messages.GameEntityJsonSerializer;
import com.wx3.cardbattle.game.messages.GameMessage;
import com.wx3.cardbattle.game.messages.IMessageHandler;

/**
 * Sends messages to the channel in the form of JSON, for example:
 * 
 * {
 *     "messageClass" : "EventMessage",
 *     "eventClass" : "ChatEvent",
 *     "event" : { "username" : "steve", "message" : "hello world!" }
 * }
 * 
 * @author Kevin
 *
 */
public class WebsocketMessageHandler implements IMessageHandler {
	
	private GamePlayer player;
	private Channel channel;
	
	public WebsocketMessageHandler(Channel channel, GamePlayer player) {
		this.channel = channel;
		this.player = player;
	}
	
	public void disconnect() {
		channel.disconnect();
	}
	
	public void handleMessage(GameMessage message) {
		GsonBuilder builder = new GsonBuilder();
		builder.registerTypeAdapter(GameEntity.class, new GameEntityJsonSerializer());
		Gson gson = builder.create();
		String encoded = gson.toJson(message);
		this.channel.writeAndFlush(new TextWebSocketFrame(encoded));
	}
}
