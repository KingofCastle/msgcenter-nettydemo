package com.qixu.msgcenter.server;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.net.InetSocketAddress;

@Component
public class TCPServer {
    protected Logger logger = LoggerFactory.getLogger(getClass());
    @Autowired
    @Qualifier("serverBootstrap")
    private ServerBootstrap protobufServerBootstrap;

    @Autowired
    @Qualifier("wsServerBootstrap")
    private ServerBootstrap wsServerBootstrap;

    @Autowired
    @Qualifier("tcpSocketAddress")
    private InetSocketAddress protobufTcpPort;

    @Autowired
    @Qualifier("wsTcpSocketAddress")
    private InetSocketAddress wsTcpPort;

    private ChannelFuture serverChannelFuture;

    private ChannelFuture wsServerChannelFuture;

    @PostConstruct
    public void start() throws Exception {
        if (protobufTcpPort != null) {
            logger.info("server start protobuf port:{}", protobufTcpPort.getPort());
            serverChannelFuture = protobufServerBootstrap.bind(protobufTcpPort).sync();
        }
        if (wsTcpPort != null) {
            logger.info("server start ws port:{}", wsTcpPort.getPort());
            wsServerChannelFuture = wsServerBootstrap.bind(wsTcpPort).sync();
        }
    }

    @PreDestroy
    public void stop() throws Exception {
        serverChannelFuture.channel().closeFuture().sync();
        wsServerChannelFuture.channel().closeFuture().sync();
    }

    public ServerBootstrap getProtobufServerBootstrap() {
        return protobufServerBootstrap;
    }

    public void setProtobufServerBootstrap(ServerBootstrap protobufServerBootstrap) {
        this.protobufServerBootstrap = protobufServerBootstrap;
    }

    public InetSocketAddress getProtobufTcpPort() {
        return protobufTcpPort;
    }

    public void setProtobufTcpPort(InetSocketAddress protobufTcpPort) {
        this.protobufTcpPort = protobufTcpPort;
    }

    public ServerBootstrap getWsServerBootstrap() {
        return wsServerBootstrap;
    }

    public void setWsServerBootstrap(ServerBootstrap wsServerBootstrap) {
        this.wsServerBootstrap = wsServerBootstrap;
    }

    public InetSocketAddress getWsTcpPort() {
        return wsTcpPort;
    }

    public void setWsTcpPort(InetSocketAddress wsTcpPort) {
        this.wsTcpPort = wsTcpPort;
    }
}
