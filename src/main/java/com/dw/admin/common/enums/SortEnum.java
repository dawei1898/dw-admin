package com.dw.admin.common.enums;



/**
 * 排序枚举
 */
public enum SortEnum {

    ASC("asc"," 升序"),
    DESC("desc","降序");

    private final String code;

    private final String message;

    SortEnum(String code, String message) {
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
