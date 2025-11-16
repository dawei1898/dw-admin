package com.dw.admin.components.storage;

import cn.hutool.core.util.IdUtil;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.components.oss.FileInfo;
import com.dw.admin.config.StorageProperties;
import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.model.COSObject;
import com.qcloud.cos.model.GetObjectRequest;
import com.qcloud.cos.model.ObjectMetadata;
import com.qcloud.cos.model.PutObjectRequest;
import com.qcloud.cos.model.PutObjectResult;
import com.qcloud.cos.region.Region;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.Date;

/**
 * 腾讯云对象存储COS服务类
 *
 * @author dw-admin
 */
@Slf4j
@Component
public class CosService implements FileStorageService {

    @Resource
    private StorageProperties storageProperties;

    /**
     * 获取COS客户端
     */
    private COSClient getCosClient() {
        StorageProperties.CosConfig cosConfig = storageProperties.getCos();
        if (cosConfig == null) {
            throw new BizException("腾讯云COS配置未找到");
        }

        COSCredentials cred = new BasicCOSCredentials(
                cosConfig.getSecretId(), 
                cosConfig.getSecretKey());
        
        Region region = new Region(cosConfig.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);
        
        return new COSClient(cred, clientConfig);
    }

    /**
     * 生成文件URL
     */
    private String generateFileUrl(String cosKey) {
        StorageProperties.CosConfig cosConfig = storageProperties.getCos();
        String region = cosConfig.getRegion();
        String bucket = cosConfig.getBucket();
        
        // 根据区域生成对应的访问域名
        String domain;
        if (region.startsWith("ap-")) {
            if (region.contains("-1")) {
                // 旧版区域格式，如 ap-guangzhou-1
                domain = bucket + ".cos." + region + ".myqcloud.com";
            } else {
                // 新版区域格式，如 ap-guangzhou
                domain = bucket + ".cos." + region + ".tencentcos.cn";
            }
        } else {
            domain = bucket + ".cos." + region + ".myqcloud.com";
        }
        
        return "https://" + domain + "/" + cosKey;
    }

    @Override
    public FileInfo uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return null;
        }

        COSClient cosClient = null;
        try {
            // 生成唯一文件名
            String contentType = multipartFile.getContentType();
            Long fileId = IdUtil.getSnowflakeNextId();
            String originalFilename = multipartFile.getOriginalFilename();
            String cosKey = storageProperties.getCos().getPrefixPath() + fileId + "/" + originalFilename;

            // 计算文件大小
            long size = multipartFile.getSize();

            // 构建上传请求
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(size);
            objectMetadata.setContentType(contentType);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    storageProperties.getCos().getBucket(),
                    cosKey,
                    multipartFile.getInputStream(),
                    objectMetadata);

            // 获取COS客户端并执行上传
            cosClient = getCosClient();
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            log.info("文件上传成功: {}, etag: {}", cosKey, putObjectResult.getETag());

            // 返回文件信息
            return FileInfo.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .size(size)
                    .type(contentType)
                    .path(cosKey)
                    .url(generateFileUrl(cosKey))
                    .build();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BizException("文件上传失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    @Override
    public void downloadFile(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return;
        }

        COSClient cosClient = null;
        try {
            cosClient = getCosClient();
            GetObjectRequest getObjectRequest = new GetObjectRequest(
                    storageProperties.getCos().getBucket(), 
                    fileKey);
            
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            ObjectMetadata objectMetadata = cosObject.getObjectMetadata();
            
            HttpServletResponse response = getResponse();
            try (InputStream inputStream = cosObject.getObjectContent()) {
                response.setContentType(objectMetadata.getContentType());
                response.setHeader("Content-Disposition", "attachment; filename=" + 
                        StringUtils.substringAfterLast(fileKey, "/"));

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            }
        } catch (Exception e) {
            log.error("文件下载失败: {}", fileKey, e);
            throw new BizException("文件下载失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    @Override
    public String getPresignedUrl(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return "";
        }

        COSClient cosClient = null;
        try {
            cosClient = getCosClient();
            // COS SDK v5 不直接支持预签名URL生成，这里返回直接访问URL
            // 在实际生产环境中，可以使用临时密钥生成预签名URL
            return generateFileUrl(fileKey);
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}", fileKey, e);
            throw new BizException("获取文件预签名URL失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    @Override
    public boolean deleteFile(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return false;
        }

        COSClient cosClient = null;
        try {
            cosClient = getCosClient();
            cosClient.deleteObject(storageProperties.getCos().getBucket(), fileKey);
            log.info("文件删除成功: {}", fileKey);
            return true;
        } catch (Exception e) {
            log.error("文件删除失败: {}", fileKey, e);
            throw new BizException("删除文件失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }

    /**
     * 获取响应
     */
    private HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }
}