package com.dw.admin.components.redis;


import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.clients.jedis.RedisClient;

/**
 * Redis 分布式锁工厂
 * 用于创建分布式锁实例
 *
 * @author dawei
 */
@Component
@ConditionalOnProperty(name = RedisConstant.REDIS_PROPERTIES_ENABLE, matchIfMissing = true)
public class RedisLockFactory {

    @Autowired
    private RedisClient redisClient;

    /**
     * 默认锁过期时间（30秒）
     */
    private static final long DEFAULT_EXPIRE_TIME = 30000L;

    /**
     * 创建分布式锁
     *
     * @param lockKey 锁的键
     * @return 分布式锁实例
     */
    public RedisDistributedLock createLock(String lockKey) {
        return createLock(lockKey, DEFAULT_EXPIRE_TIME);
    }

    /**
     * 创建分布式锁（指定过期时间）
     *
     * @param lockKey    锁的键
     * @param expireTime 锁的过期时间（毫秒）
     * @return 分布式锁实例
     */
    public RedisDistributedLock createLock(String lockKey, long expireTime) {
        return new RedisDistributedLock(redisClient, lockKey, expireTime);
    }

    /**
     * 创建分布式锁（带业务前缀）
     *
     * @param prefix     业务前缀
     * @param lockKey    锁的键
     * @param expireTime 锁的过期时间（毫秒）
     * @return 分布式锁实例
     */
    public RedisDistributedLock createLock(String prefix, String lockKey, long expireTime) {
        String fullKey = prefix + ":" + lockKey;
        return new RedisDistributedLock(redisClient, fullKey, expireTime);
    }
}
