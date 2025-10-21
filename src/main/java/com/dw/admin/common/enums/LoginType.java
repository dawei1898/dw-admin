package com.dw.admin.common.enums;


/**
 * 登录类型枚举
 */
public enum LoginType {

    EMAIL("email"),
    USERNAME("username");

    private final String code;

    LoginType(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }

}
