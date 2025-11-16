package com.dw.admin.components.storage;

import com.dw.admin.common.exception.BizException;
import com.dw.admin.components.oss.OssService;
import com.dw.admin.config.StorageProperties;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * 文件存储工厂类
 *
 * @author dw-admin
 */
@Slf4j
@Component
public class FileStorageFactory {

    @Resource
    private StorageProperties storageProperties;

    @Resource
    private OssService ossService;

    @Resource
    private CosService cosService;

    /**
     * 根据配置获取对应的存储服务实例
     *
     * @return 文件存储服务实例
     */
    public FileStorageService getStorageService() {
        String provider = storageProperties.getProvider();
        switch (provider) {
            case "aliyun":
                log.info("使用阿里云OSS文件存储服务");
                return ossService;
            case "tencent":
                log.info("使用腾讯云COS文件存储服务");
                return cosService;
            default:
                throw new BizException("不支持的存储提供商: " + provider + 
                    ", 支持的提供商: aliyun, tencent");
        }
    }
}