package com.dw.admin.components.permission;



import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * 权限配置属性
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = PermissionConstant.PERMISSION_PROPERTIES_PREFIX)
public class PermissionProperties {

    /** 是否开启权限 */
    private Boolean enable = true;

    /** 权限缓存类型 redis 或 local */
    private String cacheType = PermissionConstant.CACHE_TYPE_LOCAL;

    /** token失效时间(s) */
    private long expireTime = PermissionConstant.DEFAULT_EXPIRE_TIME;


}
