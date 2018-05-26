package com.qixu.msgcenter.util;

import com.qixu.msgcenter.service.SpringContextHolder;


public final class RedisUtil {

	private static  final String cacheName ="msgcenter";

	static RedisCache redisCache = SpringContextHolder.getBean(RedisCache.class);

	/**
	 * 向缓存中设置对象
	 * 
	 * @param key
	 * @param value
	 * @param seconds (设置为0表示使用默认值)
	 * @return
	 */
	public static void set(String key, Object value, int seconds) {
		redisCache.put(key, value, seconds);
	}

	public static void set(String key, Object value) {
		redisCache.put(key, value);
	}

	/**
	 * 根据key 获取对象
	 * 
	 * @param key
	 * @return
	 */
	public static <T> T get(String key, Class<T> clazz) {
		return redisCache.get(key, clazz);
	}

    public static void del(String key){
		redisCache.evict(key);
    }

}