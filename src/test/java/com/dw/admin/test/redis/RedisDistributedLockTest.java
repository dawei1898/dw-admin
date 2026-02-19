package com.dw.admin.test.redis;


import com.dw.admin.components.redis.RedisDistributedLock;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import redis.clients.jedis.RedisClient;
import redis.clients.jedis.params.SetParams;

import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Redis 分布式锁单元测试
 * 使用 Mockito 模拟 Jedis，不依赖实际 Redis 环境
 *
 * @author dawei
 */
@Slf4j
public class RedisDistributedLockTest {

    @Mock
    private RedisClient jedis;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    /**
     * 测试基本的加锁和解锁
     */
    @Test
    public void testBasicLockUnlock() {
        // Mock: 第一次加锁成功
        when(jedis.set(eq("test:lock:basic"), anyString(), any(SetParams.class)))
                .thenReturn("OK");
        // Mock: 解锁成功
        when(jedis.eval(anyString(), anyList(), anyList()))
                .thenReturn(1L);

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:basic", 30000L);

        // 获取锁
        boolean acquired = lock.tryLock();
        log.info("第一次尝试获取锁: {}", acquired);
        assertTrue(acquired, "应该成功获取锁");
        assertTrue(lock.isLocked(), "锁应该被标记为已持有");

        // 释放锁
        lock.unlock();
        assertFalse(lock.isLocked(), "锁应该被标记为未持有");
        log.info("锁已释放");

        // 验证 eval 被调用（解锁）
        verify(jedis, atLeastOnce()).eval(anyString(), anyList(), anyList());
    }

    /**
     * 测试加锁失败的情况
     */
    @Test
    public void testLockFailure() {
        // Mock: 加锁失败（返回 null）
        when(jedis.set(eq("test:lock:fail"), anyString(), any(SetParams.class)))
                .thenReturn(null);

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:fail", 30000L);

        boolean acquired = lock.tryLock();
        log.info("尝试获取锁: {}", acquired);
        assertFalse(acquired, "加锁应该失败");
        assertFalse(lock.isLocked(), "锁不应该被标记为已持有");
    }

    /**
     * 测试带超时的 tryLock
     */
    @Test
    public void testTryLockWithTimeout() throws InterruptedException {
        // Mock: 前几次返回 null，最后返回 OK
        when(jedis.set(eq("test:lock:timeout"), anyString(), any(SetParams.class)))
                .thenReturn(null)
                .thenReturn(null)
                .thenReturn("OK");

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:timeout", 30000L);

        log.info("开始尝试获取锁（超时时间1秒）");
        boolean acquired = lock.tryLock(1, TimeUnit.SECONDS);
        log.info("获取锁结果: {}", acquired);
        assertTrue(acquired, "应该成功获取锁");

        // 清理
        when(jedis.eval(anyString(), anyList(), anyList())).thenReturn(1L);
        lock.unlock();
    }

    /**
     * 测试带超时的 tryLock 超时失败
     */
    @Test
    public void testTryLockTimeout() throws InterruptedException {
        // Mock: 始终返回 null
        when(jedis.set(eq("test:lock:timeout-fail"), anyString(), any(SetParams.class)))
                .thenReturn(null);

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:timeout-fail", 30000L);

        log.info("开始尝试获取锁（超时时间200ms）");
        boolean acquired = lock.tryLock(200, TimeUnit.MILLISECONDS);
        log.info("获取锁结果: {}", acquired);
        assertFalse(acquired, "应该获取锁超时");
    }

    /**
     * 测试阻塞式获取锁
     */
    @Test
    public void testBlockingLock() {
        // Mock: 第一次失败，第二次成功
        when(jedis.set(eq("test:lock:blocking"), anyString(), any(SetParams.class)))
                .thenReturn(null)
                .thenReturn("OK");
        when(jedis.eval(anyString(), anyList(), anyList())).thenReturn(1L);

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:blocking", 30000L);

        log.info("开始阻塞式获取锁");
        lock.lock();
        log.info("成功获取锁");
        assertTrue(lock.isLocked(), "锁应该被标记为已持有");

        lock.unlock();
    }

    /**
     * 测试可中断的锁
     */
    @Test
    public void testLockInterruptibly() throws InterruptedException {
        // Mock: 始终返回 null，模拟锁被占用
        when(jedis.set(eq("test:lock:interruptible"), anyString(), any(SetParams.class)))
                .thenReturn(null);

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:interruptible", 30000L);

        Thread thread = new Thread(() -> {
            try {
                log.info("线程开始尝试获取锁（可中断模式）");
                lock.lockInterruptibly();
                fail("应该抛出 InterruptedException");
            } catch (InterruptedException e) {
                log.info("线程被成功中断");
            }
        });

        thread.start();
        Thread.sleep(200);

        // 中断线程
        thread.interrupt();
        log.info("发送中断信号");

        thread.join(1000);
        assertFalse(thread.isAlive(), "线程应该已结束");
    }

    /**
     * 测试自动续期功能
     */
    @Test
    public void testAutoRenewal() throws InterruptedException {
        // Mock: 加锁成功
        when(jedis.set(eq("test:lock:renewal"), anyString(), any(SetParams.class)))
                .thenReturn("OK");
        // Mock: 续期成功
        when(jedis.eval(contains("pexpire"), anyList(), anyList()))
                .thenReturn(1L);
        // Mock: 解锁成功
        when(jedis.eval(contains("del"), anyList(), anyList()))
                .thenReturn(1L);

        // 创建一个 300ms 过期的锁，续期间隔约 100ms
        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:renewal", 300L);

        boolean acquired = lock.tryLock();
        log.info("获取锁: {}", acquired);
        assertTrue(acquired, "应该成功获取锁");

        // 等待超过原始过期时间，让续期任务执行几次
        log.info("等待 500ms，让续期任务执行...");
        Thread.sleep(500);

        // 验证锁仍然被持有
        assertTrue(lock.isLocked(), "锁应该仍然被持有");
        log.info("锁仍然被持有，自动续期功能正常");

        // 验证续期脚本被调用过
        verify(jedis, atLeastOnce()).eval(contains("pexpire"), anyList(), anyList());

        // 释放锁
        lock.unlock();
        assertFalse(lock.isLocked(), "锁应该被标记为未持有");
        log.info("锁已释放");
    }

    /**
     * 测试续期失败后自动停止
     */
    @Test
    public void testRenewalFailure() throws InterruptedException {
        // Mock: 加锁成功
        when(jedis.set(eq("test:lock:renewal-fail"), anyString(), any(SetParams.class)))
                .thenReturn("OK");
        // Mock: 续期失败（锁已被其他线程持有）
        when(jedis.eval(contains("pexpire"), anyList(), anyList()))
                .thenReturn(0L);

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:renewal-fail", 300L);

        boolean acquired = lock.tryLock();
        assertTrue(acquired, "应该成功获取锁");
        assertTrue(lock.isLocked(), "锁应该被标记为已持有");

        // 等待续期任务执行
        log.info("等待续期任务执行...");
        Thread.sleep(200);

        // 续期失败后，锁应该被标记为未持有
        assertFalse(lock.isLocked(), "续期失败后锁应该被标记为未持有");
        log.info("续期失败后，锁已被自动标记为未持有");
    }

    /**测试解锁时锁已经过期或被其他线程持有
     */
    @Test
    public void testUnlockWhenLockExpiredOrHeldByOthers() {
        // Mock: 加锁成功
        when(jedis.set(eq("test:lock:unlock-fail"), anyString(), any(SetParams.class)))
                .thenReturn("OK");
        // Mock: 解锁失败（锁已被其他线程持有）
        when(jedis.eval(contains("del"), anyList(), anyList()))
                .thenReturn(0L);

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:unlock-fail", 30000L);

        lock.tryLock();
        assertTrue(lock.isLocked());

        // 解锁（应该不会抛异常）
        lock.unlock();
        assertFalse(lock.isLocked(), "解锁后应该标记为未持有");
        log.info("解锁失败但不抛异常，锁已标记为未持有");
    }

    /**
     * 测试 newCondition 抛出异常
     */
    @Test
    public void testNewConditionNotSupported() {
        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:condition", 30000L);

        assertThrows(UnsupportedOperationException.class, lock::newCondition,
                "应该抛出 UnsupportedOperationException");
        log.info("newCondition 正确抛出了 UnsupportedOperationException");
    }

    /**
     * 测试获取锁属性
     */
    @Test
    public void testLockProperties() {
        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:props", 5000L);

        assertEquals("test:lock:props", lock.getLockKey(), "lockKey 应该正确");
        assertNotNull(lock.getLockValue(), "lockValue 不应该为 null");
        assertEquals(5000L, lock.getExpireTime(), "expireTime 应该正确");
        assertFalse(lock.isLocked(), "初始状态应该未锁定");

        log.info("lockKey={}, lockValue={}, expireTime={}",
                lock.getLockKey(), lock.getLockValue(), lock.getExpireTime());
    }

    /**
     * 测试 Redis 异常时的处理
     */
    @Test
    public void testRedisException() {
        // Mock: Redis 抛出异常
        when(jedis.set(eq("test:lock:exception"), anyString(), any(SetParams.class)))
                .thenThrow(new RuntimeException("模拟 Redis 异常"));

        RedisDistributedLock lock = new RedisDistributedLock(jedis, "test:lock:exception", 30000L);

        // 应该返回 false 而不是抛异常
        boolean acquired = lock.tryLock();
        assertFalse(acquired, "Redis 异常时应该返回 false");
        log.info("Redis 异常时正确返回 false");
    }
}
