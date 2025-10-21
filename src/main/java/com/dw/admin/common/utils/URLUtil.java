package com.dw.admin.common.utils;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.net.URI;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * URL 工具类
 *
 * @author dawei
 */
@Slf4j
public class URLUtil {

    public static final String EXPIRES_NAME  = "Expires";

    /**
     * url 是否过期
     */
    public static boolean isExpired(String url) {
        if (StringUtils.isNotBlank( url)) {
            try {
                String expires =  getParamValue(url, EXPIRES_NAME);
                if (StringUtils.isNotEmpty(expires)) {
                    long expiresTime = Long.parseLong(expires);
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
        if (StringUtils.isNotEmpty(url)) {
            String expires = URLUtil.getParamValue(url, EXPIRES_NAME);
            if (StringUtils.isNumeric(expires)) {
                return Integer.parseInt(expires);
            }
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
            } catch (Exception e) {
                log.error("Failed to getParamValue.", e);
            }
        }
        return "";
    }

    /**
     * 根据带参数的 URL 获取文件名
     *
     * url = "http://dwa-oss-bucket.oss-cn-hangzhou.aliyuncs.com/dwa/1952770676166279168/dog.png?Expires=1755016584&OSSAccessKeyId=LTAI5tAx1rNrvfzX7du91kGq&Signature=xP6j36U5XaiZk9n9qwxWzlCUuaY%3D";
     */
    public static String getFileName(String url) {
        if (StringUtils.isNotBlank(url)) {
            try {
                URI uri = new URI(url);
                String path = uri.getPath();
                if (path != null) {
                    int lastSlashIndex = path.lastIndexOf('/');
                    if (lastSlashIndex != -1) {
                        return path.substring(lastSlashIndex + 1);
                    }
                }
            } catch (Exception e) {
                log.error("Failed to getFileName.", e);
            }
        }
        return "";
    }

}
