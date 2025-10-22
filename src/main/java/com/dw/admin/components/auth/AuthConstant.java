package com.dw.admin.components.auth;

/**
 * 鉴权常量
 *
 * @author dawei
 */
public class AuthConstant {

    /** 配置属性前缀 */
    public static final String  AUTH_PROPERTIES_PREFIX = "dwa.auth";

    public static final String  AUTH_PROPERTIES_ENABLE = "dwa.auth.enable";

    public static final String  AUTH_PROPERTIES_CACHE_TYPE= "dwa.auth.cache-type";

    /** 鉴权过滤器顺序 */
    public static final int AUTH_ORDER = 1;

    public static final String TOKEN_KEY = "Authorization";

    public static final String TOKEN_VALUE_PREFIX = "Bearer ";


    /** 登录用户 */
    public static final String LOGIN_USER = "login_user";

    /** 令牌秘钥 */
    public static final String DEFAULT_SECRET = "oujyu6tHmgPeCG*uYGL9JhgR5BdKzBiusBZ048iuAgfS5QP";

    /** token失效时间(s) */
    public static final long DEFAULT_EXPIRE_TIME = 24 * 60 * 60;

    /** token 存储类型 Redis */
    public static final String CACHE_TYPE_REDIS = "redis";

    /** token 存储类型 DB */
    public static final String CACHE_TYPE_DB = "DB";

    /** 清除定时任务 */
    public static final String CLEAN_CRON = "0 0 1 * * ?";




}
