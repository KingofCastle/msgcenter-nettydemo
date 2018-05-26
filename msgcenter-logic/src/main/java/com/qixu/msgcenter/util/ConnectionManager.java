package com.qixu.msgcenter.util;

import com.qixu.msgcenter.context.ChannelContext;
import com.qixu.msgcenter.exception.BizException;
import com.qixu.msgprotocol.client.transfer.*;
import io.netty.channel.Channel;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.util.concurrent.GlobalEventExecutor;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;

@PropertySource("classpath:netty-server.properties")
public class ConnectionManager {

    protected static Logger logger = LoggerFactory.getLogger(ConnectionManager.class);

    @Value("${netty.channel.readTimeout}")
    private static int readTimeout;

    public static ChannelGroup allChannels = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);

//    /**
//     * 未登录根据AppClientID+ClientSN保存连接
//     * @param protocol
//     * @param channel
//     */
//    public static void addConnection(MCProtocolPB.MCProtocol protocol, Channel channel) throws BizException {
//        StringBuffer str = new StringBuffer();
//        str.append(protocol.getPlatformID());
//        str.append(protocol.getAppClientID());
//        str.append(protocol.getClientSN());
//        if (StringUtils.isBlank(str.toString())) {
//            throw new BizException();
//        } else {
//            logger.info("add connection id:"+str.toString());
//            ChannelContext channelContext = new ChannelContext();
//            channelContext.setToMap(str.toString());
//            channelContext.setChannelId(channel.id());
//            RedisUtil.set(str.toString(), channelContext, readTimeout);
//        }
//    }

    /**
     * 登录后根据AppClientID+ClientSN保存连接
     *
     * @param protocol
     * @param userID
     * @param channel
     */
    private static void addConnection(MCProtocolPB.MCProtocol protocol, String userID, Channel channel) throws BizException {
        StringBuffer str = new StringBuffer();
        str.append(protocol.getPlatformID());
        str.append(protocol.getAppClientID());
        str.append(protocol.getClientSN());
        str.append(userID);
        if (StringUtils.isBlank(str.toString())) {
            throw new BizException();
        } else {
            logger.info("add connection id:" + str.toString());
            ChannelContext channelContext = new ChannelContext();
            channelContext.setToMap(str.toString());
            channelContext.setChannelId(channel.id());
            channelContext.setUserID(userID);
            RedisUtil.set(str.toString(), channelContext, readTimeout);
            RedisUtil.set(channel.id().asShortText(), channelContext, readTimeout);
        }
    }

    /**
     * 登录后重置保存连接
     *
     * @param protocol
     * @param userID
     * @param channel
     */
    public static void resetConnection(MCProtocolPB.MCProtocol protocol, String userID, Channel channel) throws BizException {
        removeConnection(protocol);
        removeConnection(protocol, userID);
        addConnection(protocol, userID, channel);
    }

    /**
     * 登录后重置连接时间
     *
     * @param protocol
     * @param channel
     */
    public static void resetConnectionTime(MCProtocolPB.MCProtocol protocol, Channel channel) throws BizException {
        ChannelContext context = getChannelContext(channel);
        addConnection(protocol, context.getUserID(), channel);
    }

    /**
     * 心跳重置连接时间
     *
     * @param channel
     */
    public static void resetConnectionTime(Channel channel) {
        ChannelContext channelContext = getChannelContext(channel);
        if (null == channelContext) {
            return;
        }
        Object key = channelContext.getFromMap(ChannelContext.channelKey);
        RedisUtil.set(key.toString(), channelContext, readTimeout);
        RedisUtil.set(channel.id().asShortText(), channelContext, readTimeout);
    }

    /**
     * 根据appClientID+ClientSN移除连接
     *
     * @param protocol
     */
    public static void removeConnection(MCProtocolPB.MCProtocol protocol) {
        StringBuffer str = new StringBuffer();
        str.append(protocol.getPlatformID());
        str.append(protocol.getAppClientID());
        str.append(protocol.getClientSN());
        if (StringUtils.isNotBlank(str.toString())) {
            allChannels.remove(getChannel(str.toString()));
            RedisUtil.del(str.toString());
        }
    }

    /**
     * 根据appClientID+ClientSN移除连接
     *
     * @param protocol
     */
    public static void removeConnection(MCProtocolPB.MCProtocol protocol, String userId) {
        StringBuffer str = new StringBuffer();
        str.append(protocol.getPlatformID());
        str.append(protocol.getAppClientID());
        str.append(protocol.getClientSN());
        str.append(userId);
        if (StringUtils.isNotBlank(str.toString())) {
            allChannels.remove(getChannel(str.toString()));
            RedisUtil.del(str.toString());
        }
    }

    /**
     * 根据appClientID+ClientSN获取连接
     *
     * @param protocol
     * @return
     */
    public static Channel getConnection(MCProtocolPB.MCProtocol protocol, String userId) {
        StringBuffer str = new StringBuffer();
        str.append(protocol.getPlatformID());
        str.append(protocol.getAppClientID());
        str.append(protocol.getClientSN());
        str.append(userId);
        if (StringUtils.isBlank(str.toString())) {
            return null;
        } else {
            return getChannel(str.toString());
        }
    }

    /**
     * 根据appClientID+userID获取连接
     *
     * @param appClientID
     * @param clientSN
     * @return
     */
    public static Channel getConnection(String platformID, String appClientID, String clientSN, String userId) {
        StringBuffer str = new StringBuffer();
        str.append(platformID);
        str.append(appClientID);
        str.append(clientSN);
        str.append(userId);
        if (StringUtils.isBlank(str.toString())) {
            return null;
        } else {
            return getChannel(str.toString());
        }
    }

    /**
     * 获取Channel
     *
     * @param channelIDstr
     * @return
     */
    public static Channel getChannel(String channelIDstr) {
        logger.info("get channel id:" + channelIDstr);
        ChannelContext channelContext = RedisUtil.get(channelIDstr, ChannelContext.class);
        if (channelContext == null) {
            return null;
        }
        return allChannels.find(channelContext.getChannelId());
    }

    /**
     * 获取channelcontext
     *
     * @param channelIDstr
     * @return
     */
    public static ChannelContext getChannelContext(String channelIDstr) {
        logger.info("get channel id:" + channelIDstr);
        return RedisUtil.get(channelIDstr, ChannelContext.class);
    }

    /**
     * 获取channelcontext
     *
     * @param protocol
     * @return
     */
    public static ChannelContext getChannelContext(MCProtocolPB.MCProtocol protocol, String userId) {
        StringBuffer str = new StringBuffer();
        str.append(protocol.getPlatformID());
        str.append(protocol.getAppClientID());
        str.append(protocol.getClientSN());
        str.append(userId);
        if (StringUtils.isBlank(str.toString())) {
            return null;
        } else {
            return RedisUtil.get(str.toString(), ChannelContext.class);
        }
    }

    /**
     * 获取channelcontext
     *
     * @param channel
     * @return
     */
    public static ChannelContext getChannelContext(Channel channel) {
        return RedisUtil.get(channel.id().asShortText(), ChannelContext.class);
    }

    /**
     * 清楚缓存信息
     *
     * @param protocol
     * @param channel
     */
    public static void cleanCache(MCProtocolPB.MCProtocol protocol, Channel channel) throws BizException {
        StringBuffer str = new StringBuffer();
        str.append(protocol.getPlatformID());
        str.append(protocol.getAppClientID());
        str.append(protocol.getClientSN());
        if (StringUtils.isNotBlank(str.toString())) {
            RedisUtil.del(str.toString());
            RedisUtil.del(channel.id().asShortText());
            throw new BizException();
        }
    }

    public static void closeChannelByMutilpleLogin(MCProtocolPB.MCProtocol protocol, Channel ctx) throws BizException {
        if (null == ctx) {
            return;
        }
        MCResponsePB.MCResponse mcResponse = MCResponsePB.MCResponse.newBuilder()
                .setResultCode(MCResultCodePB.MCResultCode.ROBBED_LOGIN)
                .setResultDesc("被抢登")
                .build();
        MCProtocolPB.MCProtocol mcProtocol = MCProtocolPB.MCProtocol.newBuilder()
                .setAppClientID(protocol.getAppClientID())
                .setClientSN(protocol.getClientSN())
                .setCommand(MCCommandPB.MCCommand.MUTILPLE_CLIENT_LOGIN_VALUE)
                .setDirection(DirectionPB.Direction.RESPONSE)
                .setPlatformID(protocol.getPlatformID())
                .setReqID(protocol.getReqID())
                .setToken(protocol.getToken())
                .setType(MsgTypePB.MsgType.BUSINESS_API)
                .setVersion(protocol.getVersion())
                .setBody(mcResponse.toByteString())
                .build();

        ctx.writeAndFlush(mcProtocol);

        logger.info("closeChannelByMutilpleLogin channel id:{} ClientSN:{}", ctx.id().asShortText(), protocol.getClientSN());
        throw new BizException();
    }
}
