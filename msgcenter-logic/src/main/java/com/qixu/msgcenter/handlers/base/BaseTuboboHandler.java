package com.qixu.msgcenter.handlers.base;

import com.qixu.msgcenter.context.ChannelContext;
import com.qixu.msgcenter.exception.BizException;
import com.qixu.msgcenter.util.ConnectionManager;
import com.qixu.msgprotocol.client.transfer.MCProtocolPB;
import io.netty.channel.ChannelHandlerContext;

public abstract class BaseTuboboHandler extends BaseHandler {

    public static final String CLIENT_SN_KEY = "clientsn_";

    protected void checkToken(ChannelHandlerContext ctx, MCProtocolPB.MCProtocol protocol) throws BizException {
        logger.info("--start-- protocol request id:{}", protocol.getReqID());
        verifyClientSn(protocol, ctx);
        logger.debug("protocol:{}", protocol.toString());
        ChannelContext context = ConnectionManager.getChannelContext(ctx.channel());
        logger.debug("context:" + context);
        if (null == context) {//没有拿到上下文，说明是第一次来
            //TODO
            closeChannel(protocol, ctx);
        } else {
            //TODO
        }
    }

    /**
     * 校验client_sn
     *
     * @param protocol
     * @param ctx
     */
    protected void verifyClientSn(MCProtocolPB.MCProtocol protocol, ChannelHandlerContext ctx) throws BizException {
        //TODO
    }

    /**
     * 连接建立的时候校验token不通过时关闭连接
     *
     * @param protocol
     * @param ctx
     */
    private void closeChannel(MCProtocolPB.MCProtocol protocol, ChannelHandlerContext ctx) throws BizException {

    }

    @Override
    protected void doCloseService(ChannelHandlerContext ctx, ChannelContext channelContext) {

    }
}
