package com.dw.admin.service;


import com.dw.admin.common.entity.PageResult;
import com.dw.admin.model.param.LoginParam;
import com.dw.admin.model.param.RegisterParam;
import com.dw.admin.model.param.UserPageParam;
import com.dw.admin.model.param.UserParam;
import com.dw.admin.model.vo.UserVo;

/**
 * 用户表 服务类
 *
 * @author dawei
 */
public interface UserService {

    /**
     * 注册用户
     */
    void register(RegisterParam param);

    /**
     * 用户登录
     */
    String login(LoginParam param);

    /**
     * 退出登录
     */
    void logout();

    /**
     * 保存用户
     */
    Long saveUser(UserParam param);

    /**
     * 修改当前登录用户信息
     */
    Long updateUser(UserParam param);

    /**
     * 删除用户信息
     */
    Boolean deleteUser(Long userId);

    /**
     * 查询用户信息
     */
    UserVo queryUser(Long userId);

    /**
     * 查询用户列表
     */
    PageResult<UserVo> queryUserPage(UserPageParam param);

}
