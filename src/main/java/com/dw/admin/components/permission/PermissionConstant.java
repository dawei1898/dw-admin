package com.dw.admin.components.permission;

/**
 * 权限常量
 *
 * @author dawei
 */
public class PermissionConstant {

    /** 配置属性前缀 */
    public static final String  PERMISSION_PROPERTIES_PREFIX = "dwa.permission";

    public static final String  PERMISSION_PROPERTIES_ENABLE = "dwa.permission.enable";

    public static final String  PERMISSION_PROPERTIES_CACHE_TYPE= "dwa.permission.cache-type";

    /** 权限 AOP 切面顺序 */
    public static final int PERMISSION_ORDER = 10;


    /**  权限存储类型 redis */
    public static final String CACHE_TYPE_REDIS = "redis";

    /** 权限存储类型  本地 */
    public static final String CACHE_TYPE_LOCAL = "local";

    /** 缓存失效时间(s) */
    public static final long DEFAULT_EXPIRE_TIME =  60;





}
