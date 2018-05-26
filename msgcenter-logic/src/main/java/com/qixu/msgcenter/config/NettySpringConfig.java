package com.qixu.msgcenter.config;

import com.qixu.msgcenter.handlers.StringProtocolInitializer;
import com.qixu.msgcenter.ws.WsProtocolInitializer;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.epoll.EpollEventLoopGroup;
import io.netty.channel.epoll.EpollServerSocketChannel;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.stereotype.Service;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

@Configuration
@Service
public class NettySpringConfig {

    private static Logger logger = LoggerFactory.getLogger(NettySpringConfig.class);

    @Autowired
    NettyConfig nettyConfig;
    @Autowired
    @Qualifier("springProtocolInitializer")
    private StringProtocolInitializer protocolInitializer;
    @Autowired
    @Qualifier("wsProtocolInitializer")
    private WsProtocolInitializer wsProtocolInitializer;

    @SuppressWarnings("unchecked")
    @Bean(name = "serverBootstrap")
    public ServerBootstrap bootstrap() {
        return serverBootstrap(protocolInitializer);
    }

    @SuppressWarnings("unchecked")
    @Bean(name = "wsServerBootstrap")
    public ServerBootstrap wsbootstrap() {
        return serverBootstrap(wsProtocolInitializer);
    }

    private ServerBootstrap serverBootstrap(ChannelInitializer<SocketChannel> channelChannelInitializer) {
        ServerBootstrap b = new ServerBootstrap();
        b.group(bossGroup(), workerGroup());
        if (isLinux()) {
            b.channel(EpollServerSocketChannel.class);
        } else {
            b.channel(NioServerSocketChannel.class);
        }
        b.childHandler(channelChannelInitializer);
        Map<ChannelOption<?>, Object> tcpChannelOptions = tcpChannelOptions();
        Set<ChannelOption<?>> keySet = tcpChannelOptions.keySet();
        for (@SuppressWarnings("rawtypes")
                ChannelOption option : keySet) {
            b.option(option, tcpChannelOptions.get(option));
        }
        return b;
    }

    @Bean(name = "bossGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup bossGroup() {
        if (isLinux()) {
            return new EpollEventLoopGroup(nettyConfig.getBossCount());
        } else {
            return new NioEventLoopGroup(nettyConfig.getBossCount());
        }
    }

    @Bean(name = "workerGroup", destroyMethod = "shutdownGracefully")
    public EventLoopGroup workerGroup() {
        if (isLinux()) {
            return new EpollEventLoopGroup(nettyConfig.getWorkerCount());
        } else {
            return new NioEventLoopGroup(nettyConfig.getWorkerCount());
        }
    }

    private boolean isLinux() {
        String osName = System.getProperty("os.name");
        logger.info("os name:{}", osName);
        if (StringUtils.equalsIgnoreCase(osName, "Linux")) {
            return true;

        } else {
            return false;
        }
    }

    @Bean(name = "tcpSocketAddress")
    public InetSocketAddress tcpPort() {
        int port = nettyConfig.getTcpPort();
        if (port == -1) {
            return null;
        }
        return new InetSocketAddress(port);
    }

    @Bean(name = "wsTcpSocketAddress")
    public InetSocketAddress wstcpPort() {
        int port = nettyConfig.getWsTcpPort();
        if (port == -1) {
            return null;
        }
        return new InetSocketAddress(port);
    }

    @Bean(name = "tcpChannelOptions")
    public Map<ChannelOption<?>, Object> tcpChannelOptions() {
        Map<ChannelOption<?>, Object> options = new HashMap<ChannelOption<?>, Object>();
        options.put(ChannelOption.SO_KEEPALIVE, nettyConfig.isKeepAlive());
        options.put(ChannelOption.SO_BACKLOG, nettyConfig.getBacklog());
        return options;
    }

    /**
     * Necessary to make the Value annotations work.
     *
     * @return
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertyPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
}
