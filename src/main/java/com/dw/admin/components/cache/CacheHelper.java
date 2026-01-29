package com.dw.admin.components.cache;

/**
 * 缓存服务
 *
 * @author dawei
 */
public interface CacheHelper {

    /**
     * 是否存在
     *
     * @param key 缓存键
     * @return 存在返回 true，不存在返回 false
     */
    boolean exists(String key);

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    String get(String key);


    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    void set(String key, String value);

    /**
     * 设置缓存,带过期时间
     *
     * @param key               缓存键
     * @param value             缓存值
     * @param expireTimeSeconds 过期时间（秒）
     */
    void set(String key, String value, int expireTimeSeconds);

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 删除成功返回 true，否则返回 false
     */
    boolean delete(String key);
}
