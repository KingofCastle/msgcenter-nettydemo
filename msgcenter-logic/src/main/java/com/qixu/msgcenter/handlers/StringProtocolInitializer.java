package com.qixu.msgcenter.handlers;

import com.qixu.msgcenter.handlers.base.SSLChannelInitializer;
import com.qixu.msgprotocol.client.transfer.MCProtocolPB;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.protobuf.ProtobufDecoder;
import io.netty.handler.codec.protobuf.ProtobufEncoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32FrameDecoder;
import io.netty.handler.codec.protobuf.ProtobufVarint32LengthFieldPrepender;
import io.netty.handler.ssl.SslHandler;
import io.netty.handler.timeout.ReadTimeoutHandler;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Component
@Qualifier("springProtocolInitializer")
@PropertySource("classpath:netty-server.properties")
public class StringProtocolInitializer extends SSLChannelInitializer {

    @Autowired
    ServerHandler serverHandler;

    @Value("${netty.channel.readTimeout}")
    private long readTimeout;

    @Value("${netty.channel.isSSL}")
    private boolean isSSL;

    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ChannelPipeline pipeline = ch.pipeline();
        pipeline.addLast(new ReadTimeoutHandler(readTimeout, TimeUnit.SECONDS));
        if (isSSL){
            pipeline.addLast(new SslHandler(initSSL("server.jks", "12345".toCharArray(), "client.jks", "12345".toCharArray())));
        }
        pipeline.addLast(new ProtobufVarint32FrameDecoder());
        pipeline.addLast(new ProtobufDecoder(MCProtocolPB.MCProtocol.getDefaultInstance()));
        pipeline.addLast(new ProtobufVarint32LengthFieldPrepender());
        pipeline.addLast(new ProtobufEncoder());
        pipeline.addLast(serverHandler);
    }
}
