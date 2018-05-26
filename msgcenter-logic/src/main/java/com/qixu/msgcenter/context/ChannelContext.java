package com.qixu.msgcenter.context;

import com.google.common.collect.Maps;
import io.netty.channel.ChannelId;

import java.io.Serializable;
import java.util.HashMap;


public class ChannelContext implements Serializable {

    public static final String channelKey = "channelid";

    private ChannelId channelId;
    private HashMap<String, Object> contextMap = Maps.newHashMap();
    private String userID;


    public ChannelId getChannelId() {
        return channelId;
    }

    public void setChannelId(ChannelId channelId) {
        this.channelId = channelId;
    }

    public HashMap<String, Object> getContextMap() {
        return contextMap;
    }

    public void setContextMap(HashMap<String, Object> contextMap) {
        this.contextMap = contextMap;
    }

    public String getUserID() {
        return userID;
    }

    public void setUserID(String userID) {
        this.userID = userID;
    }

    public void setToMap(String key, String value) {
        this.contextMap.put(key, value);
    }

    public void setToMap(String value) {
        this.contextMap.put(channelKey, value);
    }

    public Object getFromMap(String key) {
        return this.contextMap.get(key);
    }
}
