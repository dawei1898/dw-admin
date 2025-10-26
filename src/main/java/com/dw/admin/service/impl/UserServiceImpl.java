package com.dw.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.IdUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.enums.SortEnum;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.common.utils.PasswordUtils;
import com.dw.admin.common.utils.RequestHolder;
import com.dw.admin.common.utils.ValidateUtil;
import com.dw.admin.components.auth.*;
import com.dw.admin.dao.UserMapper;
import com.dw.admin.model.entity.DwaUser;
import com.dw.admin.model.param.LoginParam;
import com.dw.admin.model.param.RegisterParam;
import com.dw.admin.model.param.UserPageParam;
import com.dw.admin.model.param.UserParam;
import com.dw.admin.model.vo.UserVo;
import com.dw.admin.service.LoginLogService;
import com.dw.admin.service.UserService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 用户表 服务实现类
 *
 * @author dawei
 */
@Slf4j
@Service
public class UserServiceImpl implements UserService {

    @Resource
    private UserMapper usersMapper;

    @Resource
    private LoginLogService loginLogServiceImpl;

    @Resource
    private AuthProperties authProperties;




    /**
     * 注册用户
     */
    @Override
    public void register(RegisterParam param) {
        // 校验信息
        QueryWrapper<DwaUser> queryWrapper = new QueryWrapper<>();;
        queryWrapper.setEntity(DwaUser.builder().name(param.getUsername()).build());
        List<DwaUser> existUser = usersMapper.selectList(queryWrapper);
        if (CollectionUtil.isNotEmpty(existUser)) {
            throw new BizException("用户名已注册!");
        }
        // 保存用户
        DwaUser users = DwaUser.builder()
                .name(param.getUsername())
                .password(PasswordUtils.encode(param.getPassword()))
                .build();
        usersMapper.insert(users);
    }

    /**
     * 用户登录
     */
    @Override
    public String login(LoginParam param) {
        // 查询用户
        LambdaQueryWrapper<DwaUser> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(DwaUser::getName, param.getUsername());
        DwaUser user = usersMapper.selectOne(queryWrapper);
        if (user == null) {
            throw new BizException("账号不存在!");
        }
        if (!PasswordUtils.matches(param.getPassword(), user.getPassword())) {
            throw new BizException("密码不正确!");
        }

        // 生成登录用户信息
        LoginUser loginUser = LoginUser.builder()
                .tokenId(IdUtil.fastSimpleUUID())
                .userId(user.getId())
                .username(user.getName())
                .ipAddr(RequestHolder.getHttpServletRequestIpAddress())
                .loginTime(System.currentTimeMillis())
                .expireTime(System.currentTimeMillis() + authProperties.getExpireTime() * 1000)
                .build();

        // 生成token
        String token = AuthUtil.buildToken(loginUser);

        // 保存登录 log
        String userAgentStr = RequestHolder.getHeader("User-Agent");
        loginLogServiceImpl.asyncSaveLoginLog(loginUser, userAgentStr);

        return token;
    }


    /**
     * 退出登录
     */
    @Override
    public void logout() {
        LoginUser loginUser = UserContextHolder.getUser();
        if (loginUser != null) {
            String tokenId = loginUser.getTokenId();
            AuthUtil.removeToken(tokenId);
        }

        // TODO 记录退出 log
    }

    /**
     * 保存用户信息
     */
    @Override
    public Long saveUser(UserParam param) {
        ValidateUtil.isNull(param);
        Long userId = param.getId();
        // 新增用户
        if (userId == null) {
            ValidateUtil.isEmpty(param.getName(), "用户名不能为空!");
            ValidateUtil.isEmpty(param.getPassword(), "密码不能为空!");
            LambdaQueryWrapper<DwaUser> existUserQuery = new LambdaQueryWrapper<>();
            existUserQuery.eq(DwaUser::getName, param.getName());
            DwaUser existUser = usersMapper.selectOne(existUserQuery);
            ValidateUtil.isTure(existUser != null, "用户名已存在!");
            // 保存用户
            DwaUser user = DwaUser.builder()
                    .name(param.getName())
                    .password(PasswordUtils.encode(param.getPassword()))
                    .email(param.getEmail())
                    .phone(param.getPhone())
                    .avatarUrl(param.getAvatarUrl())
                    .build();
            usersMapper.insert(user);
            userId = user.getId();
        }
        // 修改用户
        else {
            DwaUser user = usersMapper.selectById(userId);
            if (user == null) {
                throw new BizException("用户不存在!");
            }
            if (StringUtils.isNotBlank(param.getEmail())) {
                user.setEmail(param.getEmail());
            }
            if (StringUtils.isNotBlank(param.getPhone())) {
                user.setPhone(param.getPhone());
            }
            if (StringUtils.isNotBlank(param.getAvatarUrl())) {
                user.setAvatarUrl(param.getAvatarUrl());
            }
            user.setUpdateTime(LocalDateTime.now());
            usersMapper.updateById(user);
        }
        return userId;
    }

    /**
     * 修改当前登录用户信息
     */
    @Override
    public Long updateUser(UserParam param) {
        ValidateUtil.isNull(param);
        Long userId = UserContextHolder.getUserId();

        // 修改用户
        DwaUser user = usersMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在!");
        }
        if (StringUtils.isNotBlank(param.getEmail())) {
            user.setEmail(param.getEmail());
        }
        if (StringUtils.isNotBlank(param.getPhone())) {
            user.setPhone(param.getPhone());
        }
        if (StringUtils.isNotBlank(param.getAvatarUrl())) {
            user.setAvatarUrl(param.getAvatarUrl());
        }
        user.setUpdateTime(LocalDateTime.now());
        usersMapper.updateById(user);
        return userId;
    }

    /**
     * 删除用户信息
     */
    @Override
    public Boolean deleteUser(Long userId) {
        UserVo user = queryUser(userId);
        if (user != null) {
            // 删除用户
            usersMapper.deleteById(userId);
            return true;
        }
        return false;
    }

    /**
     * 查询一个用户
     */
    @Override
    public UserVo queryUser(Long userId) {
        ValidateUtil.isNull(userId, "userId不能为空！");
        DwaUser user = usersMapper.selectById(userId);
        if (user == null) {
            throw new BizException("用户不存在!");
        }
        return BeanUtil.copyProperties(user, UserVo.class);
    }


    /**
     * 查询用户列表
     */
    @Override
    public PageResult<UserVo> queryUserPage(UserPageParam param) {
        ValidateUtil.isNull(param, "参数不能为空!");
        LambdaQueryWrapper<DwaUser> queryWrapper = new LambdaQueryWrapper<>();
        // 名称模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getName()),
                DwaUser::getName, param.getName());
        // 邮箱模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getEmail()),
                DwaUser::getEmail, param.getEmail());
        // 手机模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getPhone()),
                DwaUser::getPhone, param.getPhone());
        // 默认排序：更新时间降序
        if (StringUtils.isAllBlank(param.getCreateTimeSort(), param.getUpdateTimeSort())) {
            queryWrapper.orderByDesc(DwaUser::getUpdateTime);
        } else {
            // 创建时间排序
            if (SortEnum.ASC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByAsc(DwaUser::getCreateTime);
            } else if (SortEnum.DESC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByDesc(DwaUser::getCreateTime);
            }
            // 更新时间排序
            if (SortEnum.ASC.getCode().equalsIgnoreCase(param.getUpdateTimeSort())) {
                queryWrapper.orderByAsc(DwaUser::getUpdateTime);
            } else if (SortEnum.DESC.getCode().equalsIgnoreCase(param.getUpdateTimeSort())) {
                queryWrapper.orderByDesc(DwaUser::getUpdateTime);
            }
        }

        // 分页查询
        Page<DwaUser> page = new Page<>(param.getPageNum(), param.getPageSize());
        usersMapper.selectPage(page, queryWrapper);
        // 封装结果
        List<UserVo> userList = BeanUtil.copyToList(page.getRecords(), UserVo.class);
        return PageResult.build(param.getPageNum(), param.getPageSize(), page.getTotal(), userList);
    }

}
