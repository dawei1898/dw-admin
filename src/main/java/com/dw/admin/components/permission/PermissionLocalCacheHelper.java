package com.dw.admin.components.permission;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * 权限本地缓存服务
 *
 * @author dawei
 */
@Slf4j
@Component
@Primary
@ConditionalOnProperty(
        name = PermissionConstant.PERMISSION_PROPERTIES_CACHE_TYPE,
        havingValue = PermissionConstant.CACHE_TYPE_LOCAL
)
public class PermissionLocalCacheHelper implements PermissionCacheHelper{

    @Resource
    private PermissionProperties permissionProperties;


    /** token 本地缓存 */
    private Cache<String, List<String>> LOCAL_CACHE;

    @PostConstruct
    public void init() {
        log.info("init PermissionLocalCacheHelper ");

        LOCAL_CACHE = CacheBuilder.newBuilder()
                .maximumSize(100)
                .concurrencyLevel(8)
                .expireAfterWrite(permissionProperties.getExpireTime(), TimeUnit.SECONDS)
                .build();
    }

    @Override
    public List<String> getRoles(String userId) {
        if (StringUtils.isNotEmpty(userId)) {
            return LOCAL_CACHE.getIfPresent(userId);
        }
        return null;
    }

    @Override
    public void putRoles(String userId, List<String> roleCodes) {
        if (StringUtils.isNotEmpty(userId) && roleCodes != null) {
            LOCAL_CACHE.put(userId, roleCodes);
        }
    }

    @Override
    public void removeRoles(String userId) {
        if (StringUtils.isNotEmpty(userId)) {
            LOCAL_CACHE.invalidate(userId);
        }
    }

}
