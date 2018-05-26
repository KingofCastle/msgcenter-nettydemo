package com.qixu.msgcenter.wss;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

public class WebsocketServer {public static void main(String[] args) throws InterruptedException {
    EventLoopGroup bossGroup = new NioEventLoopGroup();
    EventLoopGroup workerGroup = new NioEventLoopGroup();


    try{
        ServerBootstrap serverBootstrap = new ServerBootstrap();
        serverBootstrap.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class)
                .handler(new LoggingHandler(LogLevel.INFO))
                .childHandler(new ChannelInitializer<SocketChannel>() {

                    @Override
                    protected void initChannel(SocketChannel ch) throws Exception {
                        //websocket协议本身是基于http协议的，所以这边也要使用http解编码器
                        ch.pipeline().addLast(new HttpServerCodec());
                        //以块的方式来写的处理器
                        ch.pipeline().addLast(new ChunkedWriteHandler());
                        //netty是基于分段请求的，HttpObjectAggregator的作用是将请求分段再聚合,参数是聚合字节的最大长度
                        ch.pipeline().addLast(new HttpObjectAggregator(8192));
                        //ws://server:port/context_path
                        //ws://localhost:9999/ws
                        //参数指的是contex_path
                        ch.pipeline().addLast(new WebSocketServerProtocolHandler("/ws"));
                        ch.pipeline().addLast(new WebsocketChannelHandler());
                    }
                });

        ChannelFuture channelFuture = serverBootstrap.bind(8899).sync();
        channelFuture.channel().closeFuture().sync();
    }finally{
        bossGroup.shutdownGracefully();
        workerGroup.shutdownGracefully();
    }
}
}
