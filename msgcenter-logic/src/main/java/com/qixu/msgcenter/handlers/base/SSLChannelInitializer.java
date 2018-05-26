package com.qixu.msgcenter.handlers.base;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;
import javax.net.ssl.TrustManagerFactory;
import java.security.KeyStore;

public abstract class SSLChannelInitializer extends ChannelInitializer<SocketChannel> {

    protected SSLEngine initSSL(String serverFileName, char[] serverpwd, String clientFileName, char[] clientpwd) throws Exception {
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore ks = KeyStore.getInstance(KeyStore.getDefaultType());
        ks.load(this.getClass().getResourceAsStream(getFileName(serverFileName)), serverpwd);
        kmf.init(ks, serverpwd);

        TrustManagerFactory tf = TrustManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
        KeyStore clientKs = KeyStore.getInstance(KeyStore.getDefaultType());
        clientKs.load(this.getClass().getResourceAsStream(getFileName(clientFileName)), clientpwd);
        tf.init(clientKs);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tf.getTrustManagers(), null);
        SSLEngine engine = sslContext.createSSLEngine();
        engine.setUseClientMode(false);
        engine.setNeedClientAuth(true);

        return engine;
    }

    private String getFileName(String fileName) {
        if (fileName.startsWith("/"))
            return fileName;
        else
            return "/" + fileName;
    }
}
