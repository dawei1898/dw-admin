package com.dw.admin.model.param;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.io.Serial;
import java.io.Serializable;


/**
 * 注册用户入参
 *
 * @author dawei
 */
@Data
public class RegisterParam implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 用户名 */
    @NotBlank
    private String username;

    /** 密码 */
    @NotBlank
    @Size(min = 6,  max = 15 , message = "长度大于 5，小于 16")
    private String password;

    /** 验证码 */
    private String code;


}