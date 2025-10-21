package com.dw.admin.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.enums.RolesEnum;
import com.dw.admin.common.enums.SortEnum;
import com.dw.admin.common.enums.StatusEnum;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.common.utils.ValidateUtil;
import com.dw.admin.components.auth.UserContextHolder;
import com.dw.admin.dao.RoleMapper;
import com.dw.admin.dao.UserRoleMapper;
import com.dw.admin.model.entity.DwaRole;
import com.dw.admin.model.entity.DwaUserRole;
import com.dw.admin.model.param.RolePageParam;
import com.dw.admin.model.param.RoleParam;
import com.dw.admin.model.param.UserRoleParam;
import com.dw.admin.model.vo.RoleVo;
import com.dw.admin.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 角色表 服务实现类
 * </p>
 *
 * @author dawei
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, DwaRole> implements RoleService {

    @Resource
    private RoleMapper rolesMapper;

    @Resource
    private UserRoleMapper userRoleMapper;


    /**
     * 获取角色列表
     */
    @Override
    public PageResult<RoleVo> queryRolePage(RolePageParam param) {
        ValidateUtil.isNull(param, "参数不能为空!");
        LambdaQueryWrapper<DwaRole> queryWrapper = new LambdaQueryWrapper<>();
        // 角色码模糊搜索
        queryWrapper.like(StrUtil.isNotBlank(param.getRoleCode()),
                DwaRole::getRoleCode, param.getRoleCode());
        // 名称模糊搜索
        queryWrapper.like(StrUtil.isNotBlank(param.getRoleName()),
                DwaRole::getRoleName, param.getRoleName());
        // 状态搜索
        queryWrapper.eq(StrUtil.isNotBlank(param.getStatus()),
                DwaRole::getStatus, param.getStatus());
        // 默认排序：更新时间降序
        if (StrUtil.isAllBlank(param.getCreateTimeSort(), param.getUpdateTimeSort())) {
            queryWrapper.orderByDesc(DwaRole::getUpdateTime);
        } else {
            // 创建时间排序
            if (SortEnum.ASC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByAsc(DwaRole::getCreateTime);
            } else if (SortEnum.DESC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByDesc(DwaRole::getCreateTime);
            }
            // 更新时间排序
            if (SortEnum.ASC.getCode().equalsIgnoreCase(param.getUpdateTimeSort())) {
                queryWrapper.orderByAsc(DwaRole::getUpdateTime);
            } else if (SortEnum.DESC.getCode().equalsIgnoreCase(param.getUpdateTimeSort())) {
                queryWrapper.orderByDesc(DwaRole::getUpdateTime);
            }
        }
        // 分页查询
        Page<DwaRole> page = new Page<>(param.getPageNum(), param.getPageSize());
        rolesMapper.selectPage(page, queryWrapper);
        // 封装结果
        List<RoleVo> roleVos = BeanUtil.copyToList(page.getRecords(), RoleVo.class);
        return PageResult.build(param.getPageNum(), param.getPageSize(), page.getTotal(), roleVos);
    }

    /**
     * 查询角色详情
     */
    @Override
    public RoleVo queryRole(String id) {
        ValidateUtil.isEmpty(id, "ID不能为空!");
        DwaRole roles = rolesMapper.selectById(id);
        if (roles != null) {
            return BeanUtil.copyProperties(roles, RoleVo.class);
        }
        return null;
    }

    /**
     * 保存角色
     */
    @Override
    public String saveRole(RoleParam param) {
        ValidateUtil.isNull(param, "参数不能为空!");
        ValidateUtil.isEmpty(param.getRoleCode(), "roleCode不能为空!");
        ValidateUtil.isEmpty(param.getRoleName(), "roleName不能为空!");
        // 新增校验
        if (param.getId() == null) {
            LambdaQueryWrapper<DwaRole> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DwaRole::getRoleCode, param.getRoleCode()).or()
                    .eq(DwaRole::getRoleName, param.getRoleName());
            if (rolesMapper.selectCount(queryWrapper) > 0L) {
                throw new BizException("角色码或角色名称已存在！");
            }
        }

        Long userId = UserContextHolder.getUserId();
        DwaRole roles = BeanUtil.copyProperties(param, DwaRole.class);
        if (StrUtil.isEmpty(roles.getStatus())) {
            roles.setStatus(StatusEnum.ENABLE.getCode());
        }
        roles.setCreateUser(userId);
        roles.setUpdateUser(userId);

        if (roles.getId() == null) {
            rolesMapper.insert(roles);
        }  else {
            roles.setUpdateUser(null);
            rolesMapper.updateById(roles);
        }
        return String.valueOf(roles.getId());
    }

    /**
     * 删除角色
     */
    @Override
    public Integer deleteRole(String id) {
        ValidateUtil.isEmpty(id, "ID不能为空!");
        int i = rolesMapper.deleteById(id);
        return i;
    }

    /**
     * 查询用户配置的角色列表
     */
    @Override
    public List<RoleVo> queryUserRoles(String userId) {
        ValidateUtil.isEmpty(userId, "用户ID不能为空!");

        // 查询用户关联的角色码列表
        List<DwaUserRole> userRoles = userRoleMapper.selectList
                (new LambdaQueryWrapper<DwaUserRole>().eq(DwaUserRole::getUserId, userId));
        if (CollectionUtil.isEmpty(userRoles)) {
            return List.of();
        }

        // 根据角色码查询角色
        List<String> roleCodes = userRoles.stream().map(DwaUserRole::getRoleCode).toList();
        LambdaQueryWrapper<DwaRole> queryWrapper = new LambdaQueryWrapper<DwaRole>();
        queryWrapper.in(DwaRole::getRoleCode, roleCodes);
        queryWrapper.orderByAsc(DwaRole::getRoleCode);
        List<DwaRole> roles = rolesMapper.selectList(queryWrapper);
        roles.removeIf(r -> StatusEnum.DISABLE.getCode().equals(r.getStatus()));
        return BeanUtil.copyToList(roles, RoleVo.class);
    }


    /**
     * 保存用户配置角色
     */
    @Override
    public void saveUserRoles(UserRoleParam param) {
        Long userId = param.getUserId();
        Long createUser = UserContextHolder.getUserId();

        // 删除旧角色
        int i = userRoleMapper.delete(new LambdaQueryWrapper<DwaUserRole>().eq(DwaUserRole::getUserId, userId));

        // 添加新角色
        if (CollectionUtil.isNotEmpty(param.getRoles())) {
            List<DwaUserRole> userRoles = param.getRoles().stream().map(role -> {
                return DwaUserRole.builder()
                        .roleCode(role.getRoleCode())
                        .userId(userId)
                        .createUser(createUser)
                        .build();
            }).toList();
            userRoleMapper.insert(userRoles);
        }
    }

    /**
     * 是否超级管理员
     */
    @Override
    public boolean isAdmin(Long userId) {
        List<String> roleCodes = queryRoleCodes(userId);
        if (CollectionUtil.isNotEmpty(roleCodes)) {
            for (String roleCode : roleCodes) {
                if (RolesEnum.ADMIN.getCode().equals(roleCode)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 根据用户 ID 查询角色码列表
     */
    @Override
    public List<String> queryRoleCodes(Long userId) {
        if (userId != null) {
            List<RoleVo> roleVos = queryUserRoles(String.valueOf(userId));
            if (CollectionUtil.isNotEmpty(roleVos)) {
                return roleVos.stream().map(RoleVo::getRoleCode).distinct().toList();
            }
        }
        return new ArrayList<>();
    }

}
