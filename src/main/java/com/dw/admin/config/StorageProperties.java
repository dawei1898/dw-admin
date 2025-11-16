package com.dw.admin.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置属性类
 *
 * @author dw-admin
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dwa.file.storage")
public class StorageProperties {

    /**
     * 存储提供商: aliyun, tencent
     */
    private String provider;

    /**
     * 阿里云OSS配置
     */
    private OssConfig oss;

    /**
     * 腾讯云COS配置
     */
    private CosConfig cos;

    @Data
    public static class OssConfig {
        private String accessKey;
        private String secretKey;
        private String endpoint;
        private String bucket;
        private String prefixPath;
        private Long urlExpires;
    }

    @Data
    public static class CosConfig {
        private String secretId;
        private String secretKey;
        private String region;
        private String bucket;
        private String prefixPath;
        private Long urlExpires;
    }
}