package com.dw.admin.components.cache;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheStats;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 本地缓存服务 - 基于 Guava Cache 实现
 *
 * <p>使用示例:</p>
 * <pre>{@code
 * // 1. 注入使用（推荐）
 * @Resource
 * private LocalCacheHelper localCacheHelper;
 *
 * // 2. 基本操作
 * localCacheHelper.set("user:1001", "张三");
 * String name = localCacheHelper.get("user:1001");
 *
 * // 3. 设置带过期时间的缓存
 * localCacheHelper.set("code:123456", "验证码", 300); // 5分钟过期
 *
 * // 4. 检查是否存在
 * boolean exists = localCacheHelper.exists("user:1001");
 *
 * // 5. 删除缓存
 * localCacheHelper.delete("user:1001");
 *
 * // 6. 获取缓存统计信息
 * CacheStats stats = localCacheHelper.getStats();
 * System.out.println("命中率: " + stats.hitRate());
 *
 * // 7. 获取缓存大小
 * long size = localCacheHelper.size();
 *
 * // 8. 清空所有缓存
 * localCacheHelper.clear();
 * }</pre>
 *
 * <p>配置说明:</p>
 * <pre>{@code
 * # application.yml
 * dwa:
 *   cache:
 *     enable: true      # 是否启用缓存
 *     type: local       # 缓存类型：local(本地缓存) 或 redis
 *     local:
 *       max-size: 10000           # 最大缓存数量
 *       default-expire-seconds: 3600  # 默认过期时间(秒)
 * }</pre>
 *
 * <p>特性:</p>
 * <ul>
 *   <li>基于 Guava Cache 实现，性能优秀</li>
 *   <li>线程安全，支持高并发访问</li>
 *   <li>支持 LRU 淘汰策略</li>
 *   <li>支持设置最大容量和过期时间</li>
 *   <li>支持缓存统计（命中率、加载时间等）</li>
 *   <li>自动清理过期数据</li>
 * </ul>
 *
 * @author dawei
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(
        name = CacheConstant.CACHE_PROPERTIES_CACHE_TYPE,
        havingValue = CacheConstant.CACHE_TYPE_LOCAL,
        matchIfMissing = true  // 默认使用本地缓存
)
public class LocalCacheHelper implements CacheHelper {

    /**
     * 主缓存：用于存储默认过期时间的数据
     */
    private final Cache<String, CacheValue> CACHE;

    /**
     * 默认过期时间（秒）
     */
    private static final int DEFAULT_EXPIRE_SECONDS = 3600;

    /**
     * 默认最大缓存数量
     */
    private static final int DEFAULT_MAX_SIZE = 10000;

    /**
     * 缓存值包装类，用于支持自定义过期时间
     */
    private static class CacheValue {
        private final String value;
        private final long expireTime;  // 过期时间戳（毫秒）

        public CacheValue(String value, long expireTimeMillis) {
            this.value = value;
            this.expireTime = expireTimeMillis;
        }

        public String getValue() {
            return value;
        }

        public boolean isExpired() {
            return System.currentTimeMillis() > expireTime;
        }
    }

    /**
     * 构造函数 - 初始化缓存
     */
    public LocalCacheHelper() {
        // 初始化主缓存：支持默认过期时间、最大容量限制、统计功能
        this.CACHE = CacheBuilder.newBuilder()
                .maximumSize(DEFAULT_MAX_SIZE)                          // 最大缓存数量
                .expireAfterWrite(DEFAULT_EXPIRE_SECONDS, TimeUnit.SECONDS) // 写入后过期时间
                .recordStats()                                          // 启用统计
                .build();

        log.info("LocalCacheHelper initialized with maxSize={}, defaultExpireSeconds={}",
                DEFAULT_MAX_SIZE, DEFAULT_EXPIRE_SECONDS);
    }

    /**
     * 是否存在
     *
     * @param key 缓存键
     * @return 存在返回 true，不存在返回 false
     */
    @Override
    public boolean exists(String key) {
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            CacheValue cacheValue = CACHE.getIfPresent(key);
            // 检查缓存是否存在且未过期
            return cacheValue != null && !cacheValue.isExpired();
        } catch (Exception e) {
            log.error("Failed to check exists for key: {}", key, e);
            return false;
        }
    }

    /**
     * 获取缓存
     *
     * @param key 缓存键
     * @return 缓存值，不存在返回 null
     */
    @Override
    public String get(String key) {
        if (StringUtils.isEmpty(key)) {
            return null;
        }
        try {
            CacheValue cacheValue = CACHE.getIfPresent(key);
            if (cacheValue == null) {
                return null;
            }
            // 检查是否过期
            if (cacheValue.isExpired()) {
                // 过期则删除并返回null
                CACHE.invalidate(key);
                return null;
            }
            return cacheValue.getValue();
        } catch (Exception e) {
            log.error("Failed to get local cache for key: {}", key, e);
            return null;
        }
    }

    /**
     * 设置缓存（使用默认过期时间）
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
            long expireTime = System.currentTimeMillis() + DEFAULT_EXPIRE_SECONDS * 1000L;
            CACHE.put(key, new CacheValue(value, expireTime));
            log.debug("Set local cache: key={}, value={}", key, value);
        } catch (Exception e) {
            log.error("Failed to set local cache for key: {}", key, e);
        }
    }

    /**
     * 设置缓存（自定义过期时间）
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
            long expireTime = System.currentTimeMillis() + expireTimeSeconds * 1000L;
            CACHE.put(key, new CacheValue(value, expireTime));
            log.debug("Set local cache with expire time: key={}, value={}, expireSeconds={}",
                    key, value, expireTimeSeconds);
        } catch (Exception e) {
            log.error("Failed to set local cache with expire time for key: {}", key, e);
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
        if (StringUtils.isEmpty(key)) {
            return false;
        }
        try {
            boolean existsInMain = CACHE.getIfPresent(key) != null;

            CACHE.invalidate(key);

            if (existsInMain) {
                log.debug("Deleted local cache for key: {}", key);
            }
            return existsInMain;
        } catch (Exception e) {
            log.error("Failed to delete local cache for key: {}", key, e);
            return false;
        }
    }

    /**
     * 清空所有缓存
     */
    public void clear() {
        try {
            CACHE.invalidateAll();
            log.info("Cleared all local cache");
        } catch (Exception e) {
            log.error("Failed to clear local cache", e);
        }
    }

    /**
     * 获取缓存大小
     *
     * @return 缓存中的条目数量
     */
    public long size() {
        try {
            return CACHE.size();
        } catch (Exception e) {
            log.error("Failed to get local cache size", e);
            return 0;
        }
    }

    /**
     * 获取主缓存的统计信息
     *
     * @return CacheStats 对象，包含命中率、加载时间等统计信息
     */
    public CacheStats getStats() {
        return CACHE.stats();
    }

}
