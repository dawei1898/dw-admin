package com.dw.admin.components.storage;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 文件存储 URL 工具类
 *
 * @author dawei
 */
@Slf4j
public class StorageUrlUtil {

    /** oss url 过期参数名称 */
    public static final String OSS_EXPIRES_NAME  = "Expires";

    /** cos url 过期参数名称 */
    public static final String COS_Q_SIGN_TIME  = "q-sign-time";


    /**
     * url 是否过期
     */
    public static boolean isExpired(String url) {
        if (StringUtils.isNotBlank( url)) {
            try {
                Integer expires =  getExpiresValue( url);
                if (expires != null) {
                    long expiresTime = Long.valueOf(expires);
                    long currentTime = System.currentTimeMillis() / 1000;
                    if (currentTime > expiresTime) {
                        log.info("CurrentTime:{}, ExpiresTime:{}, url 已过期.", currentTime, expiresTime);
                        return true;
                    }
                }
            } catch (Exception e) {
                log.error("Failed to isExpired. url:{}.", url, e);
            }
        }
        return false;
    }

    /**
     * 从 URL 提取过期的值
     */
    public static Integer getExpiresValue(String url) {
        try {
            if (StringUtils.isNotEmpty(url)) {
                // 阿里云对象存储 OSS
                // Expires=1755016584
                String expires = StorageUrlUtil.getParamValue(url, OSS_EXPIRES_NAME);
                if (StringUtils.isNotEmpty(expires)) {
                    if (StringUtils.isNumeric(expires)) {
                        return Integer.parseInt(expires);
                    }
                }
                // 腾讯云对象存储 COS
                // q-sign-time=1763384904;1763388504 (startTime;endTime)
                String qSignTime = StorageUrlUtil.getParamValue(url, COS_Q_SIGN_TIME);
                if (StringUtils.isNotEmpty(qSignTime)) {
                    String[] times = StringUtils.split(qSignTime, ";");
                    if (times.length == 2) {
                        return Integer.parseInt(times[1]);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Failed to getExpiresValue. url:{}. ", url, e);
        }
        return null;
    }

    /**
     * 从 URL 提取参数的值
     */
    public static String getParamValue(String url, String paramName) {
        if (!StringUtils.isAllBlank( url, paramName)) {
            try {
                // 解析整个URL
                URI uri = new URI(url);
                // 获取查询部分
                String query = uri.getQuery();
                if (StringUtils.isNotEmpty(query)) {
                    // 解码并分割查询参数
                    Map<String, String> params = new HashMap<>();
                    for (String param : query.split("&")) {
                        String[] pair = param.split("=");
                        String key = URLDecoder.decode(pair[0], StandardCharsets.UTF_8);
                        String value = "";
                        if (pair.length > 1) {
                            value = URLDecoder.decode(pair[1], StandardCharsets.UTF_8);
                        }
                        params.put(key, value);
                    }
                    // 输出特定参数值
                    return params.get(paramName);
                }
            } catch (Exception e) {
                log.error("Failed to getParamValue.", e);
            }
        }
        return "";
    }


}
