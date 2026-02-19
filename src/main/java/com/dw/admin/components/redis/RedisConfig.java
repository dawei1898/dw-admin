package com.dw.admin.components.redis;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.DefaultJedisClientConfig;
import redis.clients.jedis.RedisClient;

import java.time.Duration;

/**
 * Redis配置
 *
 * @author dawei
 */
@Slf4j
@Configuration
@EnableConfigurationProperties(RedisProperties.class)
@ConditionalOnProperty(name = RedisConstant.REDIS_PROPERTIES_ENABLE, matchIfMissing = true)
public class RedisConfig {

    @Resource
    private RedisProperties redisProperties;


    @Bean
    public RedisClient redisClient() {
        String url = redisProperties.getUrl();
        String hostPot = url.split(",")[0];
        String host = hostPot.split(":")[0];
        int port = Integer.parseInt(hostPot.split(":")[1]);

        String password = StringUtils.isNotBlank(redisProperties.getPassword())
                ? redisProperties.getPassword() : null;
        int timeout = redisProperties.getTimeout();
        DefaultJedisClientConfig jedisClientConfig = DefaultJedisClientConfig.builder()
                .password(password)
                .timeoutMillis(timeout)
                .build();

        // 连接池
        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(redisProperties.getMaxTotal());
        poolConfig.setMaxIdle(redisProperties.getMaxIdle());
        poolConfig.setMinIdle(redisProperties.getMinIdle());
        poolConfig.setMaxWait(Duration.ofMillis(redisProperties.getMaxWaitMillis()));

        RedisClient redisClient = null;
        try {
            redisClient = RedisClient.builder()
                    .hostAndPort(host, port)
                    .clientConfig(jedisClientConfig)
                    .poolConfig(poolConfig)
                    .build();
            log.info("RedisClient init finished.");
        } catch (Exception e) {
            log.error("Redis启动失败! ", e);
            //throw new RuntimeException(e);
        }
        return redisClient;
    }
}
