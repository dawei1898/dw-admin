package com.dw.admin.components.permissions;

import java.lang.annotation.*;


/**
 * 权限校验注解 Permissions
 *
 * @author dawei
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Permissions {

    /** 权限码 */
    String[] value() default  {};

    /** 角色码 */
    String[] roles();

}
