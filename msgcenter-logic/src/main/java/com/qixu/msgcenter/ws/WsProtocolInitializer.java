package com.qixu.msgcenter.ws;

import com.qixu.msgcenter.handlers.base.SSLChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.extensions.compression.WebSocketServerCompressionHandler;
import io.netty.handler.codec.stomp.StompSubframeDecoder;
import io.netty.handler.codec.stomp.StompSubframeEncoder;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Qualifier("wsProtocolInitializer")
@PropertySource("classpath:netty-server.properties")
public class WsProtocolInitializer extends SSLChannelInitializer {
    @Value("${netty.channel.readTimeout}")
    private long readTimeout;

    @Autowired
    WebSocketServerHandler webSocketServerHandler;
    @Value("${netty.channel.isSSL}")
    private boolean isSSL;

    @Override
    public void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ReadTimeoutHandler(readTimeout, TimeUnit.SECONDS));
        if (isSSL) {
            pipeline.addLast(new SslHandler(initSSL("server.jks", "12345".toCharArray(), "client.jks", "12345".toCharArray())));
        }
        pipeline.addLast(new HttpServerCodec());
        pipeline.addLast(new HttpObjectAggregator(65536));
        pipeline.addLast(new WebSocketServerCompressionHandler());
        pipeline.addLast(new StompSubframeDecoder());
        pipeline.addLast(new StompSubframeEncoder());
        pipeline.addLast(webSocketServerHandler);

    }
}
