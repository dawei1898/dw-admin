package com.dw.admin.components.permission;

import java.lang.annotation.*;


/**
 * 权限校验注解 Permission
 *
 * @author dawei
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permission {

    /** 权限码 */
    String[] value() default  {};

    /** 角色码 */
    String[] roles();

}
