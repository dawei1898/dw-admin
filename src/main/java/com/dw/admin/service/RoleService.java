package com.dw.admin.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.dw.admin.common.entity.PageResult;
import com.dw.admin.model.entity.DwaRole;
import com.dw.admin.model.param.RolePageParam;
import com.dw.admin.model.param.RoleParam;
import com.dw.admin.model.param.UserRoleParam;
import com.dw.admin.model.vo.RoleVo;

import java.util.List;

/**
 * <p>
 * 角色表 服务类
 * </p>
 *
 * @author dawei
 */
public interface RoleService extends IService<DwaRole> {

    /**
     * 获取角色列表
     */
    PageResult<RoleVo> queryRolePage(RolePageParam param);

    /**
     * 查询角色详情
     */
    RoleVo queryRole(String id);

    /**
     * 保存角色
     */
    String saveRole(RoleParam param);

    /**
     * 删除角色
     */
    Integer deleteRole(String id);


    /**
     * 查询用户配置的角色列表
     */
    List<RoleVo> queryUserRoles(String userId);

    /**
     * 保存用户配置角色
     */
    void saveUserRoles(UserRoleParam param);

    /**
     * 是否超级管理员
     */
    boolean isAdmin(Long userId);

    /**
     * 根据用户 ID 查询角色码列表
     */
    List<String> queryRoleCodes(Long userId);

}
