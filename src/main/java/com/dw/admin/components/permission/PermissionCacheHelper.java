package com.dw.admin.components.permission;

import java.util.List;

/**
 * 权限缓存服务接口
 *
 * @author dawei
 */
public interface PermissionCacheHelper {

    /**
     * 获取角色码
     */
    List<String> getRoles(String userId);

    /**
     * 保存角色码
     */
    void putRoles(String  userId, List<String> roleCodes);

    /**
     * 删除角色码
     */
    void removeRoles(String userId);

}
