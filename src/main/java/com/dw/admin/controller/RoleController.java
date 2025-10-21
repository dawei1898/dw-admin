package com.dw.admin.controller;

import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.entity.Response;
import com.dw.admin.components.auth.Auth;
import com.dw.admin.components.log.Log;
import com.dw.admin.components.permissions.Permissions;
import com.dw.admin.model.param.RolePageParam;
import com.dw.admin.model.param.RoleParam;
import com.dw.admin.model.param.UserRoleParam;
import com.dw.admin.model.vo.RoleVo;
import com.dw.admin.service.RoleService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * <p>
 * 角色表 前端控制器
 * </p>
 *
 * @author dawei
 */
@RestController
@RequestMapping("/role")
public class RoleController {

    @Resource
    private RoleService roleServiceImpl;

    /**
     * 获取角色列表
     */
    @Log
    @Auth
    @Permissions(roles = "admin")
    @PostMapping("/list")
    public Response<PageResult<RoleVo>> queryRolePage(@RequestBody RolePageParam param) {
        PageResult<RoleVo> pageResult = roleServiceImpl.queryRolePage(param);
        return Response.success(pageResult);
    }

    /**
     * 查询角色详情
     */
    @Log
    @Auth
    @Permissions(roles = "admin")
    @GetMapping("/{id}")
    public Response<RoleVo> queryRole(@PathVariable String id) {
        RoleVo roleVo = roleServiceImpl.queryRole(id);
        return Response.success(roleVo);
    }

    /**
     * 保存角色
     */
    @Log
    @Auth
    @Permissions(roles = "admin")
    @PostMapping("/save")
    public Response<String> saveRole(@RequestBody @Validated RoleParam param) {
        String id = roleServiceImpl.saveRole(param);
        return Response.success(id);
    }


    /**
     * 删除角色
     */
    @Log
    @Auth
    @Permissions(roles = "admin")
    @DeleteMapping("/delete/{id}")
    public Response<Void> deleteRole(@PathVariable String id) {
        roleServiceImpl.deleteRole(id);
        return Response.success();
    }

    /**
     * 查询用户配置角色列表
     */
    @Log
    @Auth
    @Permissions(roles = "admin")
    @GetMapping("/user/{userId}")
    public Response<List<RoleVo>> queryUserRoles(@PathVariable String userId) {
        List<RoleVo> roleList = roleServiceImpl.queryUserRoles(userId);
        return Response.success(roleList);
    }

    /**
     * 保存用户配置角色
     */
    @Log
    @Auth
    @Permissions(roles = "admin")
    @PostMapping("/user/save")
    public Response<Void> saveUserRoles(@RequestBody @Validated UserRoleParam param) {
        roleServiceImpl.saveUserRoles(param);
        return Response.success();
    }


}
