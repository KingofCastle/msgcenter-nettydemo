package com.qixu.msgcenter.handlers;

import com.qixu.msgcenter.handlers.base.BaseTuboboHandler;
import com.qixu.msgprotocol.client.transfer.MCProtocolPB;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;


@Component
@Qualifier("serverHandler")
@ChannelHandler.Sharable
public class ServerHandler extends BaseTuboboHandler {

    @Override
    protected void doService(ChannelHandlerContext ctx, Object msg) throws Exception {
        MCProtocolPB.MCProtocol protocol = (MCProtocolPB.MCProtocol) msg;
        checkToken(ctx, protocol);
    }
}
