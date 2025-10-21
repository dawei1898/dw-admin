package com.dw.admin.controller;

import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.entity.Response;
import com.dw.admin.components.auth.Auth;
import com.dw.admin.components.log.Log;
import com.dw.admin.model.param.LoginLogPageParam;
import com.dw.admin.model.vo.LoginLogVo;
import com.dw.admin.service.LoginLogService;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;

/**
 * <p>
 * 登录日志表 前端控制器
 * </p>
 *
 * @author dawei
 */
@RestController
@RequestMapping("/loginLog")
public class LoginLogController {

    @Resource
    private LoginLogService loginLogServiceImpl;


    /**
     * 获取登录日志列表
     */
    @Log
    @Auth
    @PostMapping("/list")
    public Response<PageResult<LoginLogVo>> queryLoginLogPage(@RequestBody LoginLogPageParam param) {
        PageResult<LoginLogVo> pageResult = loginLogServiceImpl.queryLoginLogPage(param);
        return Response.success(pageResult);
    }

    /**
     * 删除登录日志
     */
    @Log
    @Auth
    @DeleteMapping("/delete/{id}")
    public Response<Void> deleteLoginLog(@PathVariable String id) {
        loginLogServiceImpl.deleteLoginLog(id);
        return Response.success();
    }

}
