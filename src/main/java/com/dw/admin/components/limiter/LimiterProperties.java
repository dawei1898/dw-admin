package com.dw.admin.components.limiter;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 限流配置属性
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = LimiterConstant.LIMITER_PROPERTIES_PREFIX)
public class LimiterProperties {

    /** 是否启动 */
    private Boolean enable = true;

    /** 黑名单 Ip（多个用英文名逗号隔开）*/
    private String blackIps;

    /** 白名单 Ip（多个用英文名逗号隔开）*/
    private String whiteIps;


}