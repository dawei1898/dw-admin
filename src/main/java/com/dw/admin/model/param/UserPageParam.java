package com.dw.admin.model.param;


import com.dw.admin.common.entity.PageParam;
import lombok.Data;

import java.io.Serial;


/**
 * 用户分页查询入参
 */
@Data
public class UserPageParam extends PageParam implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    private String name;

    /** 邮箱 */
    private String email;

    /** 手机 */
    private String phone;

    /**
     * 创建时间排序（asc：升序，desc：降序）
     */
    private String createTimeSort;

    /**
     * 更新时间排序（asc：升序，desc：降序）
     */
    private String updateTimeSort;


}