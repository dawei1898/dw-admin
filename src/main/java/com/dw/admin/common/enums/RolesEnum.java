package com.dw.admin.common.enums;



/**
 * 角色枚举
 */
public enum RolesEnum {

    ADMIN("admin","超级管理员"),
    USER("user","普通用户");

    private final String code;

    private final String message;

    RolesEnum(String code, String message) {
        this.code = code;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

}
