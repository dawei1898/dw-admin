package com.dw.admin.model.param;



import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serializable;


/**
 * 角色入参
 *
 * @author dawei
 */
@Data
public class RoleParam implements Serializable {

    /**
     * 主键ID
     */
    private Long id;

    /**
     * 角色码
     */
    @NotBlank
    private String roleCode;

    /**
     * 角色名称
     */
    @NotBlank
    private String roleName;

    /**
     * 状态（1：启动，0：禁用）
     */
    private String status;


}
