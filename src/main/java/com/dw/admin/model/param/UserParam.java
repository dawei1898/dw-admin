package com.dw.admin.model.param;


import lombok.Data;

import java.io.Serial;


/**
 * 用户信息入参
 */
@Data
public class UserParam implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户ID */
    private Long id;

    /** 用户名 */
    private String name;

    /** 密码 */
    private String password;

    /** 邮箱 */
    private String email;

    /** 手机 */
    private String phone;

    /** 头像 URL */
    private String avatarUrl;


}