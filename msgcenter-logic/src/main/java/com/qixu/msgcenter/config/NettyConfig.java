package com.qixu.msgcenter.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

/**
 * @author castle
 */
@Configuration
@Service
public class NettyConfig {

    @Value("${boss.thread.count}")
    private int bossCount;

    @Value("${worker.thread.count}")
    private int workerCount;

    @Value("${tcp.port}")
    private int tcpPort;

    @Value("${ws.tcp.port}")
    private int wsTcpPort;

    @Value("${so.keepalive}")
    private boolean keepAlive;

    @Value("${so.backlog}")
    private int backlog;

    @Value("${netty.channel.isSSL}")
    private boolean enableSSL;

    @Value("${netty.kill.version}")
    private int killVersion;

    public int getKillVersion() {
        return killVersion;
    }

    public void setKillVersion(int killVersion) {
        this.killVersion = killVersion;
    }

    public int getBossCount() {
        return bossCount;
    }

    public void setBossCount(int bossCount) {
        this.bossCount = bossCount;
    }

    public int getWorkerCount() {
        return workerCount;
    }

    public void setWorkerCount(int workerCount) {
        this.workerCount = workerCount;
    }

    public int getTcpPort() {
        return tcpPort;
    }

    public void setTcpPort(int tcpPort) {
        this.tcpPort = tcpPort;
    }

    public boolean isKeepAlive() {
        return keepAlive;
    }

    public void setKeepAlive(boolean keepAlive) {
        this.keepAlive = keepAlive;
    }

    public int getBacklog() {
        return backlog;
    }

    public void setBacklog(int backlog) {
        this.backlog = backlog;
    }

    public boolean isEnableSSL() {
        return enableSSL;
    }

    public void setEnableSSL(boolean enableSSL) {
        this.enableSSL = enableSSL;
    }

    public int getWsTcpPort() {
        return wsTcpPort;
    }

    public void setWsTcpPort(int wsTcpPort) {
        this.wsTcpPort = wsTcpPort;
    }
}
