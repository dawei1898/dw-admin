package com.dw.admin.components.auth;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;

import java.util.concurrent.TimeUnit;


/**
 * token Redis 存储服务
 *
 * @author dawei
 */

@Slf4j
@Component
@ConditionalOnProperty(
        name = AuthConstant.AUTH_PROPERTIES_CACHE_TYPE,
        havingValue = AuthConstant.CACHE_TYPE_REDIS
)
public class TokenRedisCacheHelper implements TokenCacheHelper {

    @Autowired
    private AuthProperties authProperties;

    @Autowired
    private JedisPooled jedis;

    /** token 本地缓存 */
    private  Cache<String, String> LOCAL_CACHE;

    @PostConstruct
    public void init() {
        LOCAL_CACHE = CacheBuilder.newBuilder()
                        .maximumSize(100)
                        .concurrencyLevel(8)
                        .expireAfterWrite(authProperties.getExpireTime() / 2, TimeUnit.SECONDS)
                        .build();

        log.info("init TokenRedisCacheHelper");
    }

    public static final long REDIS_SUCCESS = 1L;


    /**
     * 是否存在
     */
    @Override
    public boolean contains(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            if (hasOfLocal(tokenId)) {
                return true;
            }
            if (hasOfRedis(tokenId)) {
                return true;
            }
        }
        return false;
    }

    private boolean hasOfLocal(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            return LOCAL_CACHE.getIfPresent(tokenId) != null;
        }
        return false;
    }

    private boolean hasOfRedis(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            try {
                return jedis.exists(tokenId);
            } catch (Exception e) {
                log.error("Failed to hasOfRedis", e);
            }
        }
        return false;
    }

    /**
     * 保存token
     */
    @Override
    public boolean put(String tokenId, String token) {
        if (StringUtils.isAnyEmpty(tokenId, token)) {
            return false;
        }
        saveToRedis(tokenId, token);
        saveToLocal(tokenId, token);
        return true;
    }

    private void saveToLocal(String tokenId, String token) {
        if (!StringUtils.isAnyEmpty(tokenId, token)) {
            LOCAL_CACHE.put(tokenId, token);
        }
    }

    private void saveToRedis(String tokenId, String token) {
        if (!StringUtils.isAnyEmpty(tokenId, token)) {
            try {
                jedis.setex(tokenId, authProperties.getExpireTime(), token);
            } catch (Exception e) {
                log.error("Failed to saveToRedis", e);
            }
        }
    }


    /**
     * 删除token
     */
    @Override
    public boolean remove(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            removeRedis(tokenId);
            removeLocal(tokenId);
        }
        return true;
    }

    private boolean removeLocal(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            LOCAL_CACHE.invalidate(tokenId);
        }
        return true;
    }

    private boolean removeRedis(String tokenId) {
        if (StringUtils.isNotEmpty(tokenId)) {
            try {
                return REDIS_SUCCESS == jedis.del(tokenId);
            } catch (Exception e) {
                log.error("Failed to removeRedis", e);
            }
        }
        return false;
    }

}
