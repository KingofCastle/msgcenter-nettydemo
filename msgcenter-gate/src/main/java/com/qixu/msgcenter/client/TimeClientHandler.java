package com.qixu.msgcenter.client;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TimeClientHandler extends ChannelInboundHandlerAdapter {
    protected static Logger logger = LoggerFactory.getLogger(TimeClientHandler.class);
    private int counter;
    private byte[] req;

    public TimeClientHandler() {
        this.req = ("QUERY TIME ORDER"+System.getProperty("line.separator")).getBytes();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String body = (String) msg;
        System.out.println("Now is "+body+"; the counter is : "+ ++counter);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ByteBuf message = null;
        for (int i = 0; i < 100; i++) {
            message = Unpooled.buffer(req.length);
            message.writeBytes(req);
            ctx.writeAndFlush(message);
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error(cause.getMessage(),cause);
        ctx.close();
    }
}
