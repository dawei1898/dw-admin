package com.dw.admin.controller;

import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.entity.Response;
import com.dw.admin.components.auth.Auth;
import com.dw.admin.components.auth.UserContextHolder;
import com.dw.admin.components.log.Log;
import com.dw.admin.components.permission.Permission;
import com.dw.admin.model.param.LoginParam;
import com.dw.admin.model.param.RegisterParam;
import com.dw.admin.model.param.UserPageParam;
import com.dw.admin.model.param.UserParam;
import com.dw.admin.model.vo.UserVo;
import com.dw.admin.service.UserService;
import jakarta.annotation.Resource;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 用户表 前端控制器
 *
 * @author dawei
 */
@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    private UserService userServiceImpl;


    /**
     * 注册用户
     */
    @Log
    @PostMapping("/register")
    public Response<Void> register(@RequestBody @Validated RegisterParam param){
        userServiceImpl.register(param);
        return Response.success();
    }


    /**
     * 用户登录
     */
    @Log
    @PostMapping("/login")
    public Response<String> login(@RequestBody @Validated LoginParam param){
        String token = userServiceImpl.login(param);
        return Response.success(token);
    }

    /**
     * 退出登录
     */
    @Log
    @Auth
    @DeleteMapping("/logout")
    public Response<Void> logout(){
        userServiceImpl.logout();
        return Response.success();
    }

    /**
     * 保存用户
     */
    @Log
    @Auth
    @Permission(roles = "admin")
    @PostMapping("/save")
    public Response<Long> saveUser(@RequestBody UserParam param){
        Long userId = userServiceImpl.saveUser(param);
        return Response.success(userId);
    }

    /**
     * 修改当前登录用户信息
     */
    @Log
    @Auth
    @PostMapping("/update")
    public Response<Long> updateUser(@RequestBody UserParam param){
        Long userId = userServiceImpl.updateUser(param);
        return Response.success(userId);
    }

    /**
     * 删除用户信息
     */
    @Log
    @Auth
    @Permission(roles = "admin")
    @DeleteMapping("/delete/{userId}")
    public Response<Boolean> deleteUser(@PathVariable String userId){
        Boolean deleted = userServiceImpl.deleteUser(Long.valueOf(userId));
        return Response.success(deleted);
    }

    /**
     * 查询用户信息
     */
    @Log
    @Auth
    @GetMapping("/{userId}")
    public Response<UserVo> queryUser(@PathVariable String userId){
        UserVo userVo = userServiceImpl.queryUser(Long.valueOf(userId));
        return Response.success(userVo);
    }

    /**
     * 查询当前登录用户信息
     */
    @Log
    @Auth
    @GetMapping("/query")
    public Response<UserVo> queryLoginUser(){
        Long userId = UserContextHolder.getUserId();
        UserVo userVo = userServiceImpl.queryUser(userId);
        return Response.success(userVo);
    }

    /**
     * 查询用户列表
     */
    @Log
    @Auth
    @Permission(roles = "admin")
    @PostMapping("/list")
    public Response<PageResult<UserVo>> queryUserPage(@RequestBody UserPageParam param){
        PageResult<UserVo> userVos = userServiceImpl.queryUserPage(param);
        return Response.success(userVos);
    }

}
