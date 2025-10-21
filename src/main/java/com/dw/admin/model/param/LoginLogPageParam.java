package com.dw.admin.model.param;



import com.dw.admin.common.entity.PageParam;
import lombok.Data;

import java.io.Serializable;


/**
 * 登录日志搜索入参
 *
 * @author dawei
 */
@Data
public class LoginLogPageParam extends PageParam implements Serializable {

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录 IP
     */
    private String ipAddress;

    /**
     * 登录时间排序（asc：升序，desc：降序）
     */
    private String loginTimeSort;


}
