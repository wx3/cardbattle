package com.wx3.cardbattle.netty;

import static io.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
import static io.netty.handler.codec.http.HttpHeaders.Names.HOST;
import static io.netty.handler.codec.http.HttpHeaders.Names.UPGRADE;
import static io.netty.handler.codec.http.HttpMethod.GET;
import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpResponseStatus.OK;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.HttpHeaders;
import io.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import io.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketFrame;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import io.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import io.netty.util.CharsetUtil;

import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Charsets;
import com.google.common.io.Resources;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.wx3.cardbattle.GameServer;
import com.wx3.cardbattle.datastore.AuthenticationException;
import com.wx3.cardbattle.datastore.PlayerAuthtoken;
import com.wx3.cardbattle.game.GameInstance;
import com.wx3.cardbattle.game.GamePlayer;

/**
 * A simple webserver that listens for websocket upgrade requests, sending
 * a static default page to all other requests. 
 */
public class HttpHandler extends SimpleChannelInboundHandler<Object> {
	
	final Logger logger = LoggerFactory.getLogger(HttpHandler.class);
	
	// Figure out where this belongs:
	public static final boolean SSL = false;

    private static final String WEBSOCKET_PATH = "/websocket";

    // The HTML webpage to send in response to a request:
    private String webpage;
    private NettyWebSocketServer server;
    private WebSocketServerHandshaker handshaker;
    
    private static void sendHttpResponse(
        ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        // Generate an error page if response getStatus code is not OK (200).
        if (res.getStatus().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            HttpHeaders.setContentLength(res, res.content().readableBytes());
        }

        // Send the response and close the connection if necessary.
        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!HttpHeaders.isKeepAlive(req) || res.getStatus().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location =  req.headers().get(HOST) + WEBSOCKET_PATH;
        if (SSL) {
            return "wss://" + location;
        } else {
            return "ws://" + location;
        }
    }
    
    public HttpHandler(NettyWebSocketServer server) {
    	this.server =server;
    	
    	URL url = Resources.getResource("index.html");

    	try {
			this.webpage = Resources.toString(url, Charsets.UTF_8);
		} catch (IOException e) {
			this.webpage = "No default webpage for this WebSocketServer";
		}
    }

    @Override
    public void channelRead0(ChannelHandlerContext ctx, Object msg) {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else {
        	logger.warn("Message was not HttpRequest.");
        	ctx.close();
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) {
        ctx.flush();
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.getDecoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        // Allow only GET methods.
        if (req.getMethod() != GET) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, FORBIDDEN));
            return;
        }
        
        String uri = req.getUri();
        
        // If the request is for the /websocket path, expect an Upgrade header and attempt
        // the websocket handshake:
        if(uri.equals(WEBSOCKET_PATH)) {
        	String upgrade = req.headers().get(UPGRADE);
            if(upgrade == null) {
            	sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            	return;
            }
            // Handshake
            WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
                    getWebSocketLocation(req), null, true);
            handshaker = wsFactory.newHandshaker(req);
            if (handshaker == null) {
                WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
            } else {
                handshaker.handshake(ctx.channel(), req);
                ctx.channel().pipeline().replace(HttpHandler.class, "MessageHandler", new WebsocketInitialHandler(server, handshaker));
            }
        } else {
        	sendHttpResponse(ctx, req, webpage);
        	return;
        }
    }
    
    private void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, String text) {
    	ByteBuf content = Unpooled.copiedBuffer(text.getBytes()); 
        FullHttpResponse res = new DefaultFullHttpResponse(HTTP_1_1, OK, content);

        res.headers().set(CONTENT_TYPE, "text/html; charset=UTF-8");
        HttpHeaders.setContentLength(res, content.readableBytes());

        sendHttpResponse(ctx, req, res);
    }
    
    
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
    

}
