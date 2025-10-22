package com.dw.admin.components.limiter;


import java.lang.annotation.*;


/**
 * 限流注解
 *
 * @author dawei
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Limiter {

    /**
     *  针对method 的 RateLimiter 的速率，以每秒可用的许可数来衡量
     */
    double rate() default 0.0;

    /**
     *  针对 IP + method 的 RateLimiter 的速率，以每秒可用的许可数来衡量
     */
    double rateOfIp() default 0.0;

    /**
     *  IP 或 METHOD（方法）
     */
    String type() default "";

}
