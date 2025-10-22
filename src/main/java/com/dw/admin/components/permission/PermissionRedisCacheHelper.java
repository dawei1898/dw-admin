package com.dw.admin.components.permission;

import com.alibaba.fastjson2.JSON;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import redis.clients.jedis.JedisPooled;

import java.util.List;


/**
 * 权限 Redis 缓存服务
 *
 * @author dawei
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = PermissionConstant.PERMISSION_PROPERTIES_CACHE_TYPE,
        havingValue = PermissionConstant.CACHE_TYPE_REDIS
)
public class PermissionRedisCacheHelper implements PermissionCacheHelper{

    @Resource
    private PermissionProperties permissionProperties;

    @Resource
    private JedisPooled jedis;


    @Override
    public List<String> getRoles(String userId) {
        if (StringUtils.isNotEmpty(userId)) {
            try {
                String roleStr = jedis.get(userId);
                if (StringUtils.isNotEmpty(roleStr)) {
                    return JSON.parseArray(roleStr, String.class);
                }
            } catch (Exception e) {
                log.error("Failed to getRoles", e);
            }
        }
        return null;
    }

    @Override
    public void putRoles(String userId, List<String> roleCodes) {
        if (StringUtils.isNotEmpty(userId) && roleCodes != null) {
            try {
                jedis.set(userId, JSON.toJSONString(roleCodes));
                jedis.expire(userId, permissionProperties.getExpireTime());
            } catch (Exception e) {
                log.error("Failed to putRoles", e);
            }
        }
    }

    @Override
    public void removeRoles(String userId) {
        if (StringUtils.isNotEmpty(userId)) {
            try {
                jedis.del(userId);
            } catch (Exception e) {
                log.error("Failed to removeRoles", e);
            }
        }
    }

}
