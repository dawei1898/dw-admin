package com.dw.admin.model.param;



import com.dw.admin.common.entity.PageParam;
import lombok.Data;

import java.io.Serializable;


/**
 * 角色入参
 *
 * @author dawei
 */
@Data
public class RolePageParam extends PageParam implements Serializable {

    /**
     * 角色码
     */
    private String roleCode;

    /**
     * 角色名称
     */
    private String roleName;

    /**
     * 状态（1：启动，0：禁用）
     */
    private String status;

    /**
     * 创建时间排序（asc：升序，desc：降序）
     */
    private String createTimeSort;

    /**
     * 更新时间排序（asc：升序，desc：降序）
     */
    private String updateTimeSort;


}
