package com.dw.admin.model.param;


import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.io.Serial;


/**
 * 用户登录入参
 *
 * @author dawei
 */
@Data
public class LoginParam implements java.io.Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 账号 */
    @NotBlank
    private String username;

    /** 密码 */
    @NotBlank
    private String password;

}