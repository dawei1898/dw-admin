package com.dw.admin.components.permissions;


import cn.hutool.core.collection.CollectionUtil;
import com.dw.admin.common.entity.Response;
import com.dw.admin.common.enums.RolesEnum;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.components.auth.UserContextHolder;
import com.dw.admin.service.RoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * 权限校验切面
 *
 * @author dawei
 */

@Slf4j
@Aspect
@Component
@Order(10)
public class PermissionsAspect {

    @Resource
    private RoleService roleServiceImpl;

    @Pointcut("@annotation(com.dw.admin.components.permissions.Permissions)")
    public void permissionsPointcut() {}

    @Before("permissionsPointcut() && @annotation(permissions)")
    public void doBeforeAdvice(JoinPoint joinPoint, Permissions permissions) throws Throwable {

        boolean hasPermissions = true;
        // TODO 校验用户的操作码权限

        // 校验用户的角色权限
        List<String> allowedRoleCodes = List.of(permissions.roles());
        if (CollectionUtil.isNotEmpty(allowedRoleCodes)) {
            hasPermissions = false;
            Long userId = UserContextHolder.getUserId();
            List<String> roleCodes = roleServiceImpl.queryRoleCodes(userId);
            if (CollectionUtil.isNotEmpty(roleCodes)) {
                // 超管
                if (CollectionUtil.containsAny(roleCodes, List.of(RolesEnum.ADMIN.getCode()))) {
                    return;
                }
                // 含有允许的角色
                if (CollectionUtil.containsAny(roleCodes, allowedRoleCodes)) {
                    return;
                }
            }
        }

        if (!hasPermissions) {
            throw new BizException(Response.PERMISSIONS_FAIL, "权限不足！");
        }
    }


}
