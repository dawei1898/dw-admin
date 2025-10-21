package com.dw.admin.model.param;


import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.io.Serializable;
import java.util.List;


/**
 * 角色入参
 *
 * @author dawei
 */
@Data
public class UserRoleParam implements Serializable {

    /**
     * 用户ID
     */
    @NotNull
    private Long userId;

    /**
     * 角色码
     */
    private List<RoleParam> roles;



}
