package com.dw.admin.components.permission;

import com.dw.admin.components.auth.AuthConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

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
public class PermissionLocalCacheHelper {


}
