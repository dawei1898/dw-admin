package com.dw.admin.test;

import com.dw.admin.components.cache.LocalCacheHelper;
import com.google.common.cache.CacheStats;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

/**
 * LocalCacheHelper 测试类
 *
 * @author dawei
 */
@SpringBootTest
public class LocalCacheHelperTest {

    private LocalCacheHelper cacheHelper;

    @BeforeEach
    public void setUp() {
        cacheHelper = new LocalCacheHelper();
        // 清空缓存，确保每个测试用例独立
        cacheHelper.clear();
    }

    /**
     * 测试基本的 set 和 get 操作
     */
    @Test
    public void testSetAndGet() {
        String key = "test:user:1001";
        String value = "张三";

        // 设置缓存
        cacheHelper.set(key, value);

        // 获取缓存
        String result = cacheHelper.get(key);

        // 验证
        assertNotNull(result, "缓存值不应为 null");
        assertEquals(value, result, "缓存值应该匹配");
    }

    /**
     * 测试带过期时间的 set 操作
     */
    @Test
    public void testSetWithExpireTime() throws InterruptedException {
        String key = "test:code:123456";
        String value = "验证码";
        int expireSeconds = 2; // 2 秒过期

        // 设置缓存（2秒后过期）
        cacheHelper.set(key, value, expireSeconds);

        // 立即获取，应该存在
        String result1 = cacheHelper.get(key);
        assertNotNull(result1, "缓存值应该存在");
        assertEquals(value, result1, "缓存值应该匹配");

        // 等待 3 秒
        Thread.sleep(3000);

        // 再次获取，应该已经过期
        String result2 = cacheHelper.get(key);
        assertNull(result2, "缓存值应该已过期");
    }

    /**
     * 测试 exists 方法
     */
    @Test
    public void testExists() {
        String key = "test:exists:key";
        String value = "test-value";

        // 初始状态，key 不存在
        assertFalse(cacheHelper.exists(key), "key 应该不存在");

        // 设置缓存
        cacheHelper.set(key, value);

        // 现在应该存在
        assertTrue(cacheHelper.exists(key), "key 应该存在");

        // 删除缓存
        cacheHelper.delete(key);

        // 再次检查，应该不存在
        assertFalse(cacheHelper.exists(key), "key 应该不存在");
    }

    /**
     * 测试 delete 方法
     */
    @Test
    public void testDelete() {
        String key = "test:delete:key";
        String value = "test-value";

        // 设置缓存
        cacheHelper.set(key, value);
        assertTrue(cacheHelper.exists(key), "key 应该存在");

        // 删除缓存
        boolean deleted = cacheHelper.delete(key);

        // 验证删除成功
        assertTrue(deleted, "删除应该成功");
        assertFalse(cacheHelper.exists(key), "key 不应该存在");

        // 再次删除，应该返回 false
        boolean deletedAgain = cacheHelper.delete(key);
        assertFalse(deletedAgain, "删除不存在的 key 应该返回 false");
    }

    /**
     * 测试 clear 方法
     */
    @Test
    public void testClear() {
        // 添加多个缓存项
        cacheHelper.set("key1", "value1");
        cacheHelper.set("key2", "value2");
        cacheHelper.set("key3", "value3");

        // 验证缓存大小
        assertEquals(3, cacheHelper.size(), "缓存大小应该是 3");

        // 清空缓存
        cacheHelper.clear();

        // 验证缓存已清空
        assertEquals(0, cacheHelper.size(), "缓存大小应该是 0");
        assertFalse(cacheHelper.exists("key1"), "key1 不应该存在");
        assertFalse(cacheHelper.exists("key2"), "key2 不应该存在");
        assertFalse(cacheHelper.exists("key3"), "key3 不应该存在");
    }

    /**
     * 测试 size 方法
     */
    @Test
    public void testSize() {
        // 初始大小应该是 0
        assertEquals(0, cacheHelper.size(), "初始大小应该是 0");

        // 添加缓存
        cacheHelper.set("key1", "value1");
        assertEquals(1, cacheHelper.size(), "大小应该是 1");

        cacheHelper.set("key2", "value2");
        assertEquals(2, cacheHelper.size(), "大小应该是 2");

        // 删除一个
        cacheHelper.delete("key1");
        assertEquals(1, cacheHelper.size(), "大小应该是 1");

        // 清空
        cacheHelper.clear();
        assertEquals(0, cacheHelper.size(), "大小应该是 0");
    }

    /**
     * 测试 getStats 方法
     */
    @Test
    public void testGetStats() {
        // 设置一些缓存
        cacheHelper.set("key1", "value1");
        cacheHelper.set("key2", "value2");

        // 进行一些 get 操作（命中）
        cacheHelper.get("key1");
        cacheHelper.get("key2");

        // 进行一些 get 操作（未命中）
        cacheHelper.get("key3");
        cacheHelper.get("key4");

        // 获取统计信息
        CacheStats stats = cacheHelper.getStats();

        // 验证统计信息
        assertNotNull(stats, "统计信息不应为 null");
        assertEquals(2, stats.hitCount(), "命中次数应该是 2");
        assertEquals(2, stats.missCount(), "未命中次数应该是 2");
        assertEquals(4, stats.requestCount(), "请求次数应该是 4");
    }

    /**
     * 测试空 key 的处理
     */
    @Test
    public void testNullOrEmptyKey() {
        // 空字符串
        assertFalse(cacheHelper.exists(""), "空 key exists 应该返回 false");
        assertNull(cacheHelper.get(""), "空 key get 应该返回 null");
        assertFalse(cacheHelper.delete(""), "空 key delete 应该返回 false");

        // set 操作不应抛出异常
        cacheHelper.set("", "value");
        cacheHelper.set("", "value", 100);
    }

    /**
     * 测试并发访问
     */
    @Test
    public void testConcurrentAccess() throws InterruptedException {
        final int threadCount = 10;
        final int operationsPerThread = 100;
        Thread[] threads = new Thread[threadCount];

        // 创建多个线程并发写入和读取
        for (int i = 0; i < threadCount; i++) {
            final int threadIndex = i;
            threads[i] = new Thread(() -> {
                for (int j = 0; j < operationsPerThread; j++) {
                    String key = "thread:" + threadIndex + ":key:" + j;
                    String value = "value:" + j;

                    // 写入
                    cacheHelper.set(key, value);

                    // 读取
                    String result = cacheHelper.get(key);
                    assertEquals(value, result, "并发读取的值应该匹配");

                    // 删除部分数据
                    if (j % 2 == 0) {
                        cacheHelper.delete(key);
                    }
                }
            });
        }

        // 启动所有线程
        for (Thread thread : threads) {
            thread.start();
        }

        // 等待所有线程完成
        for (Thread thread : threads) {
            thread.join();
        }

        // 验证缓存大小（应该是偶数索引的数据被删除了）
        long expectedSize = threadCount * operationsPerThread / 2;
        assertEquals(expectedSize, cacheHelper.size(), "并发操作后缓存大小应该正确");
    }
}
