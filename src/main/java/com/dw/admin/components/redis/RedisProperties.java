package com.dw.admin.components.redis;


import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * Redis配置属性
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = RedisConstant.REDIS_PROPERTIES_PREFIX)
public class RedisProperties {

    /** 是否开启配置Redis */
    private Boolean enable = true;

    /** 连接URL */
    private String url;

    /** 密码 */
    private String password;

    private int maxTotal = 8;

    private int maxIdle = 8;

    private int minIdle = 0;

    private int maxWaitMillis = 2000;

    private int timeout = 2000;


}
