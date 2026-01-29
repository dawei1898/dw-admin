package com.dw.admin.components.redis;


import lombok.extern.slf4j.Slf4j;
import redis.clients.jedis.JedisPooled;
import redis.clients.jedis.params.SetParams;

import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * 基于 Redis 的分布式锁实现
 * 实现 java.util.concurrent.locks.Lock 接口
 * 支持自动续期（看门狗机制）
 *
 * @author dawei
 */
@Slf4j
public class RedisDistributedLock implements Lock {

    private final JedisPooled jedis;
    private final String lockKey;
    private final String lockValue;
    private final long expireTime; // 锁的过期时间（毫秒）

    // 自动续期相关
    private final AtomicBoolean isLocked = new AtomicBoolean(false);
    private volatile ScheduledFuture<?> renewalFuture;
    private static final ScheduledExecutorService RENEWAL_EXECUTOR =
            Executors.newScheduledThreadPool(Runtime.getRuntime().availableProcessors(),
                    r -> {
                        Thread t = new Thread(r, "redis-lock-renewal");
                        t.setDaemon(true);
                        return t;
                    });

    // Lua 脚本用于原子性删除锁
    private static final String UNLOCK_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) " +
                    "else " +
                    "return 0 " +
                    "end";

    // Lua 脚本用于原子性续期锁
    private static final String RENEWAL_SCRIPT =
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('pexpire', KEYS[1], ARGV[2]) " +
                    "else " +
                    "return 0 " +
                    "end";

    /**
     * 构造函数
     *
     * @param jedis      Jedis 客户端
     * @param lockKey    锁的键
     * @param expireTime 锁的过期时间（毫秒）
     */
    public RedisDistributedLock(JedisPooled jedis, String lockKey, long expireTime) {
        this.jedis = jedis;
        this.lockKey = lockKey;
        this.lockValue = UUID.randomUUID().toString();
        this.expireTime = expireTime;
    }

    /**
     * 获取锁（阻塞式）
     * 如果锁不可用，则当前线程将一直等待直到获取到锁
     */
    @Override
    public void lock() {
        while (true) {
            try {
                if (tryLock()) {
                    return;
                }
                // 自旋等待，避免过度占用 CPU
                Thread.sleep(50);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                log.error("获取锁时被中断", e);
            }
        }
    }

    /**
     * 获取锁（可中断）
     * 如果当前线程被中断，则抛出 InterruptedException
     */
    @Override
    public void lockInterruptibly() throws InterruptedException {
        while (true) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("线程已被中断");
            }
            if (tryLock()) {
                return;
            }
            // 自旋等待
            Thread.sleep(50);
        }
    }

    /**
     * 尝试获取锁（非阻塞）
     *
     * @return true 表示获取成功，false 表示获取失败
     */
    @Override
    public boolean tryLock() {
        try {
            // 使用 SET NX EX 命令实现原子性加锁
            SetParams params = new SetParams()
                    .nx()  // 只有键不存在时才设置
                    .px(expireTime);  // 设置过期时间（毫秒）

            String result = jedis.set(lockKey, lockValue, params);
            boolean acquired = "OK".equals(result);

            if (acquired) {
                isLocked.set(true);
                // 启动自动续期任务
                startRenewalTask();
                log.debug("成功获取锁: lockKey={}, lockValue={}", lockKey, lockValue);
            }

            return acquired;
        } catch (Exception e) {
            log.error("尝试获取锁失败: lockKey={}", lockKey, e);
            return false;
        }
    }

    /**
     * 启动自动续期任务
     * 续期间隔为过期时间的 1/3
     */
    private void startRenewalTask() {
        long renewalInterval = expireTime / 3;
        renewalFuture = RENEWAL_EXECUTOR.scheduleAtFixedRate(() -> {
            try {
                if (isLocked.get()) {
                    renewLock();
                }
            } catch (Exception e) {
                log.error("续期任务执行异常: lockKey={}", lockKey, e);
            }
        }, renewalInterval, renewalInterval, TimeUnit.MILLISECONDS);
        log.debug("启动自动续期任务: lockKey={}, interval={}ms", lockKey, renewalInterval);
    }

    /**
     * 续期锁
     * 使用 Lua 脚本原子性地续期
     */
    private void renewLock() {
        try {
            Object result = jedis.eval(
                    RENEWAL_SCRIPT,
                    Collections.singletonList(lockKey),
                    java.util.Arrays.asList(lockValue, String.valueOf(expireTime))
            );

            if (Long.valueOf(1).equals(result)) {
                log.debug("成功续期锁: lockKey={}, lockValue={}, expireTime={}ms", lockKey, lockValue, expireTime);
            } else {
                // 锁已经不属于当前持有者，停止续期
                log.warn("续期失败，锁已不属于当前持有者: lockKey={}, lockValue={}", lockKey, lockValue);
                stopRenewalTask();
                isLocked.set(false);
            }
        } catch (Exception e) {
            log.error("续期锁时发生异常: lockKey={}", lockKey, e);
        }
    }

    /**
     * 停止自动续期任务
     */
    private void stopRenewalTask() {
        if (renewalFuture != null && !renewalFuture.isCancelled()) {
            renewalFuture.cancel(false);
            log.debug("停止自动续期任务: lockKey={}", lockKey);
        }
    }

    /**
     * 尝试获取锁（带超时时间）
     *
     * @param time 等待时间
     * @param unit 时间单位
     * @return true 表示获取成功，false 表示超时
     */
    @Override
    public boolean tryLock(long time, TimeUnit unit) throws InterruptedException {
        long startTime = System.currentTimeMillis();
        long timeout = unit.toMillis(time);

        while (System.currentTimeMillis() - startTime < timeout) {
            if (Thread.currentThread().isInterrupted()) {
                throw new InterruptedException("线程已被中断");
            }

            if (tryLock()) {
                return true;
            }

            // 自旋等待，避免过度占用 CPU
            Thread.sleep(50);
        }

        log.debug("获取锁超时: lockKey={}, timeout={}ms", lockKey, timeout);
        return false;
    }

    /**
     * 释放锁
     * 使用 Lua 脚本确保只有持有锁的线程才能释放锁
     */
    @Override
    public void unlock() {
        try {
            // 先停止续期任务
            stopRenewalTask();
            isLocked.set(false);

            // 使用 Lua 脚本原子性地检查并删除锁
            Object result = jedis.eval(
                    UNLOCK_SCRIPT,
                    Collections.singletonList(lockKey),
                    Collections.singletonList(lockValue)
            );

            if (Long.valueOf(1).equals(result)) {
                log.debug("成功释放锁: lockKey={}, lockValue={}", lockKey, lockValue);
            } else {
                log.warn("释放锁失败，锁可能已经过期或被其他线程持有: lockKey={}, lockValue={}", lockKey, lockValue);
            }
        } catch (Exception e) {
            log.error("释放锁时发生异常: lockKey={}", lockKey, e);
        }
    }

    /**
     * 不支持 Condition
     * 分布式锁暂不支持 Condition 机制
     *
     * @throws UnsupportedOperationException 不支持此操作
     */
    @Override
    public Condition newCondition() {
        throw new UnsupportedOperationException("分布式锁不支持 Condition");
    }

    /**
     * 获取锁的键
     */
    public String getLockKey() {
        return lockKey;
    }

    /**
     * 获取锁的值
     */
    public String getLockValue() {
        return lockValue;
    }

    /**
     * 获取锁的过期时间
     */
    public long getExpireTime() {
        return expireTime;
    }

    /**
     * 判断锁是否被当前实例持有
     */
    public boolean isLocked() {
        return isLocked.get();
    }
}
