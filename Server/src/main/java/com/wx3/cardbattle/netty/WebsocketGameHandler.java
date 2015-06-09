package com.wx3.cardbattle.netty;

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
import com.wx3.cardbattle.GameServer;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;
import com.wx3.cardbattle.game.commands.GameCommand;
import com.wx3.cardbattle.game.commands.JsonCommandFactory;
import com.wx3.cardbattle.game.messages.CommandResponseMessage;

/**
 * Handles communications from an authenticated user
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

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg)
	{
		WebSocketFrame frame = (WebSocketFrame) msg;
		if (frame instanceof CloseWebSocketFrame) {
			System.err.printf("%s closed websocket", ctx.channel());
            ctx.close();
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
			logger.error("Invalid websocket frame");
		}
	}

	@Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
