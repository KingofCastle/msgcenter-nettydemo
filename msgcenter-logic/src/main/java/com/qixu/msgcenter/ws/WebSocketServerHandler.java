package com.qixu.msgcenter.ws;

import com.qixu.msgcenter.handlers.base.BaseTuboboHandler;
import com.qixu.msgcenter.util.ConnectionManager;
import com.qixu.msgprotocol.client.transfer.MCProtocolPB;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.http.DefaultFullHttpResponse;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.codec.http.websocketx.*;
import io.netty.util.CharsetUtil;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

import static io.netty.handler.codec.http.HttpResponseStatus.BAD_REQUEST;
import static io.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
import static io.netty.handler.codec.http.HttpUtil.isKeepAlive;
import static io.netty.handler.codec.http.HttpUtil.setContentLength;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;
import static io.netty.handler.codec.stomp.StompHeaders.HOST;

@Component
@ChannelHandler.Sharable
public class WebSocketServerHandler extends BaseTuboboHandler {

    // websocket 服务的 uri
    private static final String WEBSOCKET_PATH = "/ws";

    private WebSocketServerHandshaker handshaker;

    @Override
    protected void doService(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FullHttpRequest) {
            handleHttpRequest(ctx, (FullHttpRequest) msg);
        } else if (msg instanceof WebSocketFrame) {
            handleWebSocketFrame(ctx, (WebSocketFrame) msg);
        }
    }

    private void handleHttpRequest(ChannelHandlerContext ctx, FullHttpRequest req) {
        // Handle a bad request.
        if (!req.decoderResult().isSuccess()) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, BAD_REQUEST));
            return;
        }

        if ("/favicon.ico".equals(req.uri()) || ("/".equals(req.uri()))) {
            sendHttpResponse(ctx, req, new DefaultFullHttpResponse(HTTP_1_1, NOT_FOUND));
            return;
        }


        // Handshake
        WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, true);
        handshaker = wsFactory.newHandshaker(req);
        if (handshaker == null) {
            WebSocketServerHandshakerFactory.sendUnsupportedVersionResponse(ctx.channel());
        } else {
            ChannelFuture channelFuture = handshaker.handshake(ctx.channel(), req);

            // 握手成功之后,业务逻辑
            if (channelFuture.isSuccess()) {
                logger.info("握手成功:, channel: " + ctx.channel().id().asShortText());
                ConnectionManager.allChannels.add(ctx.channel());
            }
        }
    }

    private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) throws Exception {

        if (frame instanceof CloseWebSocketFrame) {
            handshaker.close(ctx.channel(), (CloseWebSocketFrame) frame.retain());
            return;
        }
        if (frame instanceof PingWebSocketFrame) {
            ConnectionManager.resetConnectionTime(ctx.channel());
            responePong(ctx, frame);
            return;
        }
        if (frame instanceof TextWebSocketFrame){
            System.out.println("收到消息："+((TextWebSocketFrame)frame).text());
            ctx.channel().writeAndFlush(new TextWebSocketFrame("服务时间："+ LocalDateTime.now()));
            return;
        }
        if (!(frame instanceof BinaryWebSocketFrame)) {
            throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
        }
        BinaryWebSocketFrame binaryWebSocketFrame = (BinaryWebSocketFrame) frame;
        byte[] by = new byte[frame.content().readableBytes()];
        binaryWebSocketFrame.content().readBytes(by);
        readData(ctx, by);
    }

    private void responePong(ChannelHandlerContext ctx, WebSocketFrame frame) {
        ctx.channel().writeAndFlush(new PongWebSocketFrame(frame.content().retain()));
    }

    private static void sendHttpResponse(ChannelHandlerContext ctx, FullHttpRequest req, FullHttpResponse res) {
        if (res.status().code() != 200) {
            ByteBuf buf = Unpooled.copiedBuffer(res.status().toString(), CharsetUtil.UTF_8);
            res.content().writeBytes(buf);
            buf.release();
            setContentLength(res, res.content().readableBytes());
        }

        ChannelFuture f = ctx.channel().writeAndFlush(res);
        if (!isKeepAlive(req) || res.status().code() != 200) {
            f.addListener(ChannelFutureListener.CLOSE);
        }
    }

    private static String getWebSocketLocation(FullHttpRequest req) {
        String location = req.headers().get(HOST) + WEBSOCKET_PATH;
        return "ws://" + location;
    }

    private void readData(ChannelHandlerContext ctx, byte[] bytes) throws Exception {
        MCProtocolPB.MCProtocol protocol = MCProtocolPB.MCProtocol.parseFrom(bytes);
        checkToken(ctx, protocol);
    }

}
