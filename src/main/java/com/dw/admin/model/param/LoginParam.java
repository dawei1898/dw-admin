package com.dw.admin.model.param;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
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
    @Size(min = 6, max = 15, message = "密码长度在 6-15 之间")
    private String password;

}