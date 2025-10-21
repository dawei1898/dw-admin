package com.dw.admin.service;


import com.dw.admin.common.entity.PageResult;
import com.dw.admin.components.auth.LoginUser;
import com.dw.admin.model.param.LoginLogPageParam;
import com.dw.admin.model.vo.LoginLogVo;

/**
 * <p>
 * 登录日志表 服务类
 * </p>
 *
 * @author dawei
 */
public interface LoginLogService {

    /**
     * 保存登录日志
     */
    void asyncSaveLoginLog(LoginUser loginUser, String userAgentStr);

    /**
     * 获取登录日志列表
     */
    PageResult<LoginLogVo> queryLoginLogPage(LoginLogPageParam param);

    /**
     * 删除登录日志
     */
    Integer deleteLoginLog(String id);
}
