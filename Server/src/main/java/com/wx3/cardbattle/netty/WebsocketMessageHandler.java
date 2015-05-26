package com.wx3.cardbattle.netty;

import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.wx3.cardbattle.game.GameEntity;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.gameevents.GameEvent;
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
