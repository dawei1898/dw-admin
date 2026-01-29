package com.dw.admin.components.cache;



import com.dw.admin.components.permission.PermissionConstant;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


/**
 * 缓存配置属性
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = CacheConstant.CACHE_PROPERTIES_PREFIX)
public class CacheProperties {

    /** 是否开启缓存 */
    private Boolean enable = true;

    /** 缓存类型 local(默认) 或 redis */
    private String type = CacheConstant.CACHE_TYPE_LOCAL;


}
