
package com.qixu.msgcenter.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.cache.RedisCacheElement;
import org.springframework.data.redis.cache.RedisCacheKey;
import org.springframework.data.redis.core.RedisOperations;

import java.util.Set;

public class RedisCache extends org.springframework.data.redis.cache.RedisCache {
    static Logger logger = LoggerFactory.getLogger(RedisCache.class);

    private final RedisOperations redisOperations;
    private final String prefix;

    /**
     * Constructs a new <code>RedisCache</code> instance.
     *
     * @param name            cache name
     * @param prefix
     * @param redisOperations
     * @param expiration
     */
    public RedisCache(String name, String prefix, RedisOperations<?, ?> redisOperations, long expiration) {
        super(name, prefix.getBytes(), redisOperations, expiration);
        this.redisOperations = redisOperations;
        this.prefix = prefix;
    }

    @Override
    public void evict(Object key) {
        RedisCacheKey redisKey = new RedisCacheKey(key).usePrefix(this.prefix.getBytes()).withKeySerializer(
                redisOperations.getKeySerializer());
        Set<String> keys = redisOperations.keys(new String(redisKey.getKeyBytes()));
        for (String keyd : keys) {
            RedisCacheElement redisCacheElement = new RedisCacheElement(new RedisCacheKey(keyd).usePrefix(null).withKeySerializer(
                    redisOperations.getKeySerializer()), null);
            super.evict(redisCacheElement);
        }
    }

    public void put(Object key, Object value, long expired) {
        put(new RedisCacheElement(getRedisCacheKey(key), value)
                .expireAfter(expired));
    }

    private RedisCacheKey getRedisCacheKey(Object key) {
        return new RedisCacheKey(key).usePrefix(this.prefix.getBytes())
                .withKeySerializer(redisOperations.getKeySerializer());
    }

}
