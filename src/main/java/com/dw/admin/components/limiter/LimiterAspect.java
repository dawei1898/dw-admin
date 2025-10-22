package com.dw.admin.components.limiter;


import com.dw.admin.common.exception.BizException;
import com.dw.admin.common.utils.RequestHolder;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.util.concurrent.RateLimiter;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

/**
 * 限流切面
 *
 * @author dawei
 */

@Slf4j
@Aspect
@Component
@Order(LimiterConstant.LIMITER_ORDER)
@ConditionalOnProperty(
        name = LimiterConstant.LIMITER_PROPERTIES_ENABLE,
        matchIfMissing = true
)
public class LimiterAspect {


    private static final String METHOD_FORMAT = "%s#%s";

    // 使用缓存存储 IP -> RateLimiter 映射（自动过期）
    Cache<String, RateLimiter> limiterCache = CacheBuilder.newBuilder()
            .expireAfterAccess(10, TimeUnit.MINUTES)
            .build();

    @Autowired
    private LimiterProperties limiterProperties;


    @Pointcut("@annotation(com.dw.admin.components.limiter.Limiter)")
    public void limiterPointcut() {
    }


    @Around(value = "limiterPointcut() && @annotation(limiterAnnotation)")
    public Object doAroundAdvice(ProceedingJoinPoint pjp, Limiter limiterAnnotation) throws Throwable{
        String clientIp = RequestHolder.getHttpServletRequestIpAddress();
        // 白名单 IP
        if (StringUtils.contains(limiterProperties.getWhiteIps(), clientIp)) {
            return pjp.proceed();
        }
        // 黑名单 IP
        if (StringUtils.contains(limiterProperties.getBlackIps(), clientIp)) {
            log.error("请求受限，请稍后再试！");
            throw new BizException("请求受限，请稍后再试！");
        }

        String key = limiterAnnotation.rateOfIp() != 0.0 ? (getMethod(pjp) + clientIp) : getMethod(pjp);
        double rate = limiterAnnotation.rateOfIp() != 0.0 ? limiterAnnotation.rateOfIp() : limiterAnnotation.rate();

        RateLimiter rateLimiter = limiterCache.get(key, () -> RateLimiter.create(rate));
        if (rateLimiter.tryAcquire()) {
            return pjp.proceed();
        } else {
            log.error("资源不足，请稍后再试！");
            throw new BizException("资源不足，请稍后再试！");
        }
    }

    private String getMethod(ProceedingJoinPoint pjp) {
        String className = pjp.getTarget().getClass().getName();
        String methodName = pjp.getSignature().getName();
        return String.format(METHOD_FORMAT, className, methodName);
    }

}
