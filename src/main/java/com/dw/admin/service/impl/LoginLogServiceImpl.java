package com.dw.admin.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.http.useragent.UserAgent;
import cn.hutool.http.useragent.UserAgentUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.enums.SortEnum;
import com.dw.admin.common.utils.ValidateUtil;
import com.dw.admin.components.auth.LoginUser;
import com.dw.admin.components.log.Log;
import com.dw.admin.dao.LoginLogMapper;
import com.dw.admin.model.entity.DwaLoginLog;
import com.dw.admin.model.param.LoginLogPageParam;
import com.dw.admin.model.vo.LoginLogVo;
import com.dw.admin.service.LoginLogService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

/**
 * <p>
 * 登录日志表 服务实现类
 * </p>
 *
 * @author dawei
 */
@Slf4j
@Service
public class LoginLogServiceImpl implements LoginLogService {

    @Resource
    private LoginLogMapper loginLogMapper;


    /**
     * 保存登录日志
     *
     * @param loginUser 登录信息
     * @param userAgentStr userAgent：Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/140.0.0.0 Safari/537.36
     */
    @Log
    @Async
    @Override
    public void asyncSaveLoginLog(LoginUser loginUser, String userAgentStr) {
        if (loginUser == null) {
            return;
        }

        try {
            // 从 userAgent 中提取浏览器和操作系统信息
            // 操作系统
            String os =  "";
            // 浏览器
            String browser =  "";
            if (StringUtils.isNotEmpty(userAgentStr)) {
                UserAgent userAgent = UserAgentUtil.parse(userAgentStr);
                browser = String.format("%s %s", userAgent.getBrowser(), userAgent.getVersion());
                os = String.format("%s %s %s", userAgent.getPlatform(), userAgent.getOs(), userAgent.getOsVersion());
            }

            // 记录登录log
            DwaLoginLog loginLog = DwaLoginLog.builder()
                    .userId(loginUser.getUserId())
                    .username(loginUser.getUsername())
                    .ipAddr(loginUser.getIpAddress())
                    .loginLocation("")
                    .loginTime(LocalDateTime.now())
                    .os(os)
                    .browser(browser)
                    .status("success")
                    .build();
            loginLogMapper.insert(loginLog);
            log.info("记录登录log");
        } catch (Exception e) {
            log.error("记录登录log异常：", e);
        }
    }

    /**
     * 获取登录日志列表
     */
    @Override
    public PageResult<LoginLogVo> queryLoginLogPage(LoginLogPageParam param) {
        ValidateUtil.isNull(param, "参数不能为空!");
        LambdaQueryWrapper<DwaLoginLog> queryWrapper = new LambdaQueryWrapper<>();
        // 用户名模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getUsername()),
                DwaLoginLog::getUsername, param.getUsername());
        // 登录 IP 糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getIpAddress()),
                DwaLoginLog::getIpAddr, param.getIpAddress());

        // 登录时间排序
        if (SortEnum.ASC.getCode().equalsIgnoreCase(param. getLoginTimeSort())) {
            queryWrapper.orderByAsc(DwaLoginLog::getLoginTime);
        } else {
            queryWrapper.orderByDesc(DwaLoginLog::getLoginTime);
        }
        // 分页查询
        Page<DwaLoginLog> page = new Page<>(param.getPageNum(), param.getPageSize());
        loginLogMapper.selectPage(page, queryWrapper);
        // 封装结果
        List<LoginLogVo> loginLogVos = BeanUtil.copyToList(page.getRecords(), LoginLogVo.class);
        return PageResult.build(param.getPageNum(), param.getPageSize(), page.getTotal(), loginLogVos);
    }


    /**
     * 删除登录日志
     */
    @Override
    public Integer deleteLoginLog(String id) {
        ValidateUtil.isEmpty(id, "ID不能为空!");
        int i = loginLogMapper.deleteById(id);
        return i;
    }
}
