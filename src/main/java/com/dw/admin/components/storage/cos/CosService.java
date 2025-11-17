package com.dw.admin.components.storage.cos;

import cn.hutool.core.util.IdUtil;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.components.storage.FileStorageService;
import com.dw.admin.components.storage.StorageConstant;
import com.dw.admin.components.storage.FileInfo;
import com.dw.admin.components.storage.StorageProperties;
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
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Date;

/**
 * 腾讯云对象存储COS服务类
 *
 * 参考文档: <a href="https://cloud.tencent.com/document/product/436/10199" /a>
 * @author dawei
 */
@Slf4j
@Component
@ConditionalOnProperty(
        name = StorageConstant.STORAGE_PROVIDER_KEY,
        havingValue = StorageConstant.PROVIDER_TENCENT_COS
)
public class CosService implements FileStorageService {

    @Resource
    private StorageProperties storageProperties;

    /**
     * 上传文件
     *
     * @param multipartFile 前端传入文件信息
     * @return 文件信息
     */
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
            String cosKey = storageProperties.getTencentCos().getPrefixPath() + fileId + "_" + originalFilename;

            // 计算文件大小
            long size = multipartFile.getSize();

            // 构建上传请求
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(size);
            objectMetadata.setContentType(contentType);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    storageProperties.getTencentCos().getBucketName(),
                    cosKey,
                    multipartFile.getInputStream(),
                    objectMetadata);

            // 获取COS客户端并执行上传
            cosClient = getCosClient();
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            log.info("文件上传成功: {}, etag: {}", cosKey, putObjectResult.getETag());

            // 获取预览URL
            String presignedUrl = getPresignedUrl(cosKey);

            // 返回文件信息
            return FileInfo.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .size(size)
                    .type(contentType)
                    .path(cosKey)
                    .url(presignedUrl)
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


    /**
     * 上传本地文件
     *
     * @param file 本地文件信息
     * @return 文件存储 url
     */
    @Override
    public FileInfo uploadFile(File file) {
        if (file == null) {
            return null;
        }

        COSClient cosClient = null;
        try {
            // 生成唯一文件名
            String contentType = "";
            Long fileId = IdUtil.getSnowflakeNextId();
            String originalFilename = StringUtils.substringBefore(file.getName(), "?");;
            String cosKey = storageProperties.getTencentCos().getPrefixPath() + fileId + "_" + originalFilename;

            // 计算文件大小
            long size = file.getTotalSpace();

            // 构建上传请求
            ObjectMetadata objectMetadata = new ObjectMetadata();
            objectMetadata.setContentLength(size);
            //objectMetadata.setContentType(contentType);

            PutObjectRequest putObjectRequest = new PutObjectRequest(
                    storageProperties.getTencentCos().getBucketName(),
                    cosKey,
                    new FileInputStream(file),
                    objectMetadata);

            // 获取COS客户端并执行上传
            cosClient = getCosClient();
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);

            log.info("本地文件上传成功: {}, etag: {}", cosKey, putObjectResult.getETag());

            // 获取预览URL
            String presignedUrl = getPresignedUrl(cosKey);

            // 返回文件信息
            return FileInfo.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .size(size)
                    .type(contentType)
                    .path(cosKey)
                    .url(presignedUrl)
                    .build();
        } catch (Exception e) {
            log.error("本地文件上传失败", e);
            throw new BizException("本地文件上传失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }


    /**
     * 下载文件
     *
     * @param fileKey 文件路径
     */
    @Override
    public void downloadFile(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return;
        }

        COSClient cosClient = null;
        try {
            cosClient = getCosClient();
            GetObjectRequest getObjectRequest = new GetObjectRequest(
                    storageProperties.getTencentCos().getBucketName(),
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

    /**
     * 获取文件预签名URL
     *
     * @param fileKey 文件路径
     * @return 文件预签名URL: https://abc-test-1384986123.cos.ap-beijing.myqcloud.com/%E9%A6%99%E8%95%89banana.png?q-sign-algorithm=sha1&q-ak=AKIDIU8K0ucGP&q-sign-time=1763384904;1763388504&q-key-time=1763384904;1763388504&q-header-list=host&q-url-param-list=ci-process&q-signature=404b023a&x-cos-security-token=nAzXGnporj7&ci-process=originImage
     */
    @Override
    public String getPresignedUrl(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return "";
        }

        COSClient cosClient = null;
        try {
            cosClient = getCosClient();
            String bucketName = storageProperties.getTencentCos().getBucketName();
            Date expiration = DateUtils.addSeconds(new Date(), storageProperties.getTencentCos().getUrlExpires());
            URL url = cosClient.generatePresignedUrl(bucketName, fileKey, expiration);
            log.info("获取预签名URL成功: {}", url);
            return url.toString();
            // COS SDK v5 不直接支持预签名URL生成，这里返回直接访问URL
            // 在实际生产环境中，可以使用临时密钥生成预签名URL
            //return generateFileUrl(fileKey);
        } catch (Exception e) {
            log.error("获取预签名URL失败: {}", fileKey, e);
            throw new BizException("获取文件预签名URL失败: " + e.getMessage());
        } finally {
            if (cosClient != null) {
                cosClient.shutdown();
            }
        }
    }


    /**
     * 删除文件
     *
     * @param fileKey 文件路径
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return false;
        }

        COSClient cosClient = null;
        try {
            cosClient = getCosClient();
            cosClient.deleteObject(storageProperties.getTencentCos().getBucketName(), fileKey);
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
     * 获取COS客户端
     */
    private COSClient getCosClient() {
        StorageProperties.CosConfig cosConfig = storageProperties.getTencentCos();
        if (cosConfig == null) {
            throw new BizException("腾讯云COS配置未找到");
        }

        COSCredentials cred = new BasicCOSCredentials(
                cosConfig.getSecretId(), cosConfig.getSecretKey()
        );

        Region region = new Region(cosConfig.getRegion());
        ClientConfig clientConfig = new ClientConfig(region);

        return new COSClient(cred, clientConfig);
    }

    /**
     * 生成文件URL https://abc-test-1384986123.cos.ap-beijing.myqcloud.com/%E9%A6%99%E8%95%89banana.png
     */
    private String generateFileUrl(String cosKey) {
        StorageProperties.CosConfig cosConfig = storageProperties.getTencentCos();
        String region = cosConfig.getRegion();
        String bucket = cosConfig.getBucketName();

        // 根据区域生成对应的访问域名
        String urlFormat = "https://%s.cos.%s.myqcloud.com/%s";
        return String.format(urlFormat, bucket, region, cosKey);
    }

    /**
     * 获取响应
     */
    private HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }
}