package com.dw.admin.common.enums;



/**
 * 状态枚举
 */
public enum StatusEnum {

    ENABLE("1","启用"),
    DISABLE("0","禁用");

    private final String code;

    private final String message;

    StatusEnum(String code, String message) {
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
