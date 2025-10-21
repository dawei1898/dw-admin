package com.dw.admin.components.redis;


import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.ConnectionPoolConfig;
import redis.clients.jedis.JedisPooled;

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
    public JedisPooled jedis() {

        String hostPot = redisProperties.getUrl().split(",")[0];
        String host = hostPot.split(":")[0];
        int port = Integer.parseInt(hostPot.split(":")[1]);

        String password = redisProperties.getPassword();
        int timeout = redisProperties.getTimeout();

        ConnectionPoolConfig poolConfig = new ConnectionPoolConfig();
        poolConfig.setMaxTotal(redisProperties.getMaxTotal());
        poolConfig.setMaxIdle(redisProperties.getMaxIdle());
        poolConfig.setMinIdle(redisProperties.getMinIdle());
        poolConfig.setMaxWait(Duration.ofMillis(redisProperties.getMaxWaitMillis()));

        JedisPooled jedis = null;
        try {
            if (StringUtils.isEmpty(password)) {
                jedis = new JedisPooled(poolConfig, host, port);
                log.info("Jedis init finished.");
            } else {
                jedis = new JedisPooled(poolConfig, host, port, timeout, password);
                log.info("Jedis init finished whit password.");
            }
        } catch (Exception e) {
            log.error("Redis启动失败! ", e);
            //throw new RuntimeException(e);
        }
        return jedis;
    }
}
