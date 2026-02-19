package com.dw.admin.components.permission;

import java.util.List;

/**
 * 角色提供者接口
 *
 * @author dawei
 */
public interface IRoleProvider {

    /**
     * 根据用户 ID 查询角色码列表
     */
    List<String> queryRoleCodes(Long userId);

}
