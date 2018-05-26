package com.qixu.msgcenter.handlers.base;

import com.qixu.msgcenter.context.ChannelContext;
import com.qixu.msgcenter.util.ConnectionManager;
import com.qixu.msgcenter.util.RedisUtil;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseHandler extends ChannelInboundHandlerAdapter {

    protected Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelRegistered:{}", ctx.channel().id().asShortText());
        super.channelRegistered(ctx);
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelActive:{}, {}", ctx.channel().id().asShortText(), ctx.channel().remoteAddress());
        ConnectionManager.allChannels.add(ctx.channel());
        super.channelActive(ctx);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelInactive:{}", ctx.channel().id().asShortText());
        super.channelInactive(ctx);
    }

    @Override
    public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
        logger.info("channelUnregistered:{}", ctx.channel().id().asShortText());
        doClosed(ctx);
        super.channelUnregistered(ctx);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.info("{}, exceptionCaught:{}", ctx.channel().id().asShortText(), cause);
        doClosed(ctx);
        super.exceptionCaught(ctx, cause);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        try {
            // Do something with msg
            logger.info("channelRead:{}", ctx.channel().id().asShortText());
            logger.debug("channelRead:{}", ctx.channel());
            doService(ctx, msg);
        } finally {
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * 读取数据要做的业务操作
     * @param ctx
     * @param msg
     */
    protected abstract void doService(ChannelHandlerContext ctx, Object msg) throws Exception;

    /**
     * 关闭连接时需要处理的业务
     * @param ctx
     * @param channelContext
     */
    protected abstract void doCloseService(ChannelHandlerContext ctx, ChannelContext channelContext);

    protected void doClosed(ChannelHandlerContext ctx) {
        try {
            String id = ctx.channel().id().asShortText();
            ChannelContext channelContext = RedisUtil.get(id, ChannelContext.class);
            if (null == channelContext){
                return;
            }
            doCloseService(ctx, channelContext);
        } catch (Exception e) {
            logger.error("", e);
        } finally {
            ConnectionManager.allChannels.remove(ctx.channel());
            ctx.close();
        }
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt)
            throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent)evt;
            if (e.state() == IdleState.READER_IDLE) {
                doClosed(ctx);
            }
        } else {
            super.userEventTriggered(ctx, evt);
        }
    }
}
