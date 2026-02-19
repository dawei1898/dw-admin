package com.dw.admin.components.cache;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.clients.jedis.RedisClient;


/**
 *  Redis 缓存服务
 *
 * @author dawei
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = CacheConstant.CACHE_PROPERTIES_CACHE_TYPE,
        havingValue = CacheConstant.CACHE_TYPE_REDIS
)
public class RedisCacheHelper implements CacheHelper {


    @Autowired
    private RedisClient redisClient;


    /**
     * 是否存在
     *
     * @param key 缓存键
     * @return 存在返回 true，不存在返回 false
     */
    @Override
    public boolean exists(String key) {
        if (StringUtils.isNotEmpty(key)) {
            try {
                return redisClient.exists(key);
            } catch (Exception e) {
                log.error("Failed to check exists for key: {}", key, e);
            }
        }
        return false;
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值
     */
    @Override
    public String get(String key) {
        if (StringUtils.isNotEmpty(key)) {
            try {
                return redisClient.get(key);
            } catch (Exception e) {
                log.error("Failed to get redis cache for key: {}", key, e);
            }
        }
        return null;
    }

    /**
     * 设置缓存
     *
     * @param key   缓存键
     * @param value 缓存值
     */
    @Override
    public void set(String key, String value) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        try {
            redisClient.set(key, value);
        } catch (Exception e) {
            log.error("Failed to set redis cache for key: {}", key, e);
        }
    }

    /**
     * 设置缓存,带过期时间
     *
     * @param key               缓存键
     * @param value             缓存值
     * @param expireTimeSeconds 过期时间（秒）
     */
    @Override
    public void set(String key, String value, int expireTimeSeconds) {
        if (StringUtils.isEmpty(key)) {
            return;
        }
        try {
            redisClient.setex(key, expireTimeSeconds, value);
        } catch (Exception e) {
            log.error("Failed to set redis cache with expire time for key: {}", key, e);
        }
    }

    /**
     * 删除缓存
     *
     * @param key 缓存键
     * @return 删除成功返回 true，否则返回 false
     */
    @Override
    public boolean delete(String key) {
        if (StringUtils.isNotEmpty(key)) {
            try {
                Long result = redisClient.del(key);
                return result > 0;
            } catch (Exception e) {
                log.error("Failed to delete redis cache for key: {}", key, e);
            }
        }
        return false;
    }
}
