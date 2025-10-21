package com.dw.admin.components.log;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.dw.admin.common.entity.Response;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.common.utils.AddressUtil;
import com.dw.admin.common.utils.DateUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;



/**
 * 日志追踪切面
 *
 * 1.打印入参、调用方 IP
 * 2.打印返参、接口调用时间
 * 3.logbak 需同时加入 [%X{traceId}] 占位符
 *
 *  @author dawei
 */
@Aspect
@Component
@Order(-100)
public class LogAspect {

    private static final Logger logger = LoggerFactory.getLogger(LogAspect.class);

    /**
     * 日志追踪TraceId key （同时在 logback 中配置, [%X{traceId}] 占位符）
     */
    public static final String TRACE_ID = "traceId";

    private static final String METHOD_FORMAT = "%s#%s";


    @Pointcut("@annotation(com.dw.admin.components.log.Log)")
    public void LogPointcut(){
    }

    @Around("LogPointcut()  && @annotation(logAnnotation)")
    public Object doAroundAdvice(ProceedingJoinPoint pjp, Log logAnnotation) throws Throwable {
        long start = System.currentTimeMillis();
        // 开始日志追踪
        boolean first = false;
        if (StringUtils.isEmpty(MDC.get(TRACE_ID))) {
            MDC.put(TRACE_ID, IdUtil.getSnowflake().nextIdStr());
            first = true;
        }

        try {
            // 打印入参
            printArgs(pjp, logAnnotation);
            // 后置通知开始
            Object returnObj = pjp.proceed();
            //打印返参
            printReturn(pjp, logAnnotation, returnObj, start);
            return returnObj;
        } catch (Throwable throwable) {
            // 异常处理
            if (throwable instanceof BizException) {
                BizException e = (BizException) throwable;
                Response<Void> response = new Response<>();
                Integer errorCode = e.getCode() != null ? e.getCode() : Response.FAIL;
                response.setCode(errorCode);
                response.setMessage(e.getMessage());
                printReturn(pjp, logAnnotation  , response, start);
            } else {
                printThrowable(pjp, start, throwable);
            }
            throw throwable;
        } finally {
            // 清除日志追踪
            if (first) {
                MDC.remove(TRACE_ID);
            }
        }
    }


    /**
     * 打印入参
     */
    private void printArgs(ProceedingJoinPoint pjp, Log LogAnnotation) {
        try {
            if (LogAnnotation == null) {
                logger.warn("printArgs: LogAnnotation is null.");
                return;
            }
            String remoteIp = AddressUtil.getRemoteIP();
            String argsStr = JSON.toJSONString(pjp.getArgs());
            String method = getMethod(pjp);
            logger.info("==>> remoteIp:{}, method:{}, params:{}", remoteIp, method, argsStr);
        } catch (Exception e) {
            logger.error("Failed to printArgs.", e);
        }
    }


    /**
     * 打印返参
     */
    private void printReturn(ProceedingJoinPoint pjp, Log LogAnnotation, Object returnObj, long startTime) {
        try {
            if (LogAnnotation == null) {
                logger.warn("printReturn: LogAnnotation is null.");
                return;
            }
            Object returnObjStr = JSON.toJSONString(returnObj);
            String method = getMethod(pjp);
            String useTime = DateUtil.getUseTime(startTime);
            logger.info("<<== method:{}, response:{}, useTime:{}s.", method, returnObjStr, useTime);
        } catch (Exception e) {
            logger.error("Failed to printReturn.", e);
        }
    }

    /**
     * 打印异常
     */
    private void printThrowable(ProceedingJoinPoint proceedingJoinPoint, long start, Throwable throwable) {
        String method = getMethod(proceedingJoinPoint);
        String useTime = DateUtil.getUseTime(start);
        logger.info("<<== method:{}, invoke exception !!! useTime:{}s.", method, useTime, throwable);
    }

    private String getMethod(ProceedingJoinPoint pjp) {
        String className = pjp.getTarget().getClass().getName();
        String methodName = pjp.getSignature().getName();
        return String.format(METHOD_FORMAT, className, methodName);
    }

}
