package com.dw.admin.components.storage;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 文件存储配置属性类
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = StorageConstant.STORAGE_PROPERTIES_PREFIX)
public class StorageProperties {

    /**
     * 存储提供商: aliyun-oss (阿里云对象存储), tencent-cos (腾讯云对象存储)
     */
    private String provider;

    /**
     * 阿里云OSS配置
     */
    private OssConfig aliyunOss;

    /**
     * 腾讯云COS配置
     */
    private CosConfig tencentCos;

    @Data
    public static class OssConfig {
        /** AccessKey */
        private String accessKey;
        /** SecretKey */
        private String secretKey;
        /** OSS 区域节点（如：oss-cn-hangzhou.aliyuncs.com）*/
        private String endpoint;
        /** 存储桶名称 */
        private String bucketName;
        /** 前缀路径 */
        private String prefixPath;
        /** url过期时间（s） */
        private Integer urlExpires;
    }

    @Data
    public static class CosConfig {
        private String secretId;
        private String secretKey;
        /** 区域节点（如：ap-guangzhou）*/
        private String region;
        /** 存储桶名称 */
        private String bucketName;
        /** 前缀路径 */
        private String prefixPath;
        /** url过期时间（s） */
        private Integer urlExpires;
    }
}