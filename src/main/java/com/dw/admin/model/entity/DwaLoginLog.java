package com.dw.admin.model.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * <p>
 * 登录日志表
 * </p>
 *
 * @author dawei
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("dwa_login_log")
public class DwaLoginLog implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /**
     * 主键ID
     */
    @TableId(value = "id", type = IdType.ASSIGN_ID)
    private Long id;

    /**
     * 用户 ID
     */
    private Long userId;

    /**
     * 用户名
     */
    private String username;

    /**
     * 登录 IP
     */
    private String ipAddr;

    /**
     * 登录IP归属地
     */
    private String loginLocation;

    /**
     * 登录时间
     */
    private LocalDateTime loginTime;

    /**
     * 操作系统
     */
    private String os;

    /**
     * 浏览器
     */
    private String browser;

    /**
     * 登录状态（success：登录成功，fail：登录失败）
     */
    private String status;


}
