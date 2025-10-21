package com.dw.admin.components.oss;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * 阿里云对象存储 OSS 属性
 *
 * @author dawei
 */
@Data
@Configuration
@ConfigurationProperties(prefix = "dwa.file.oss")
public class OssProperties {

    /** AccessKey */
    private String accessKey;

    /** SecretKey */
    private String secretKey;

    /** OSS 区域节点（如：oss-cn-hangzhou.aliyuncs.com）*/
    private String endpoint;

    /** 存储桶名称 */
    private String bucket;

    /** 前缀路径 */
    private String prefixPath;

    /** url过期时间（s） */
    private Long urlExpires;

}
