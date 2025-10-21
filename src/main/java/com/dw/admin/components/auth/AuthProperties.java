package com.dw.admin.components.auth;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * 鉴权配置属性
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = AuthConstant.AUTH_PROPERTIES_PREFIX)
public class AuthProperties {

    /** 是否开启鉴权 */
    private Boolean enable = true;

    /** 令牌秘钥 */
    private String secret;

    /** token 存储类型 redis 或 DB */
    private String cacheType = AuthConstant.CACHE_TYPE_DB;

    /** token失效时间(s) */
    private long expireTime = AuthConstant.DEFAULT_EXPIRE_TIME;

    /** token DB缓存清除定时任务 */
    private String cleanDBCacheCron = AuthConstant.CLEAN_CRON;

}
