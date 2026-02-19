package com.dw.admin.service.impl;

import com.dw.admin.components.permission.IRoleProvider;
import com.dw.admin.service.RoleService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;

import org.springframework.stereotype.Service;
import java.util.List;

/**
 *  角色提供者
 *
 * @author dawei
 */

@Slf4j
@Service
public class RoleProvider implements IRoleProvider {

    @Resource
    private RoleService roleServiceImpl;

    @Override
    public List<String> queryRoleCodes(Long userId) {
        return roleServiceImpl.queryRoleCodes(userId);
    }
}
