package com.dw.admin.components.storage.oss;

import cn.hutool.core.util.IdUtil;
import com.alibaba.fastjson2.JSON;
import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClientBuilder;
import com.aliyun.oss.model.OSSObject;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;
import com.aliyun.oss.model.VoidResult;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.components.storage.FileInfo;
import com.dw.admin.components.storage.FileStorageService;
import com.dw.admin.components.storage.StorageConstant;
import com.dw.admin.components.storage.StorageProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;


import java.io.File;
import java.io.InputStream;
import java.util.Date;

/**
 * 文件存储服务
 * <p>
 * 阿里云 OSS 文档：https://help.aliyun.com/zh/oss/developer-reference/objects-8/?spm=a2c4g.11186623.help-menu-31815.d_18_2_0_1.7844480chHNsDe&scm=20140722.H_609604._.OR_help-T_cn~zh-V_1
 *
 * @author dawei
 */

@Slf4j
@Component
@Primary
@ConditionalOnProperty(
        name = StorageConstant.STORAGE_PROVIDER_KEY,
        havingValue = StorageConstant.PROVIDER_ALIYUN_OSS
)
public class OssService implements FileStorageService {

    @Resource
    private StorageProperties storageProperties;

    /**
     * 上传文件
     *
     * @param multipartFile 前端传入文件信息
     * @return 文件存储 url
     */
    @Override
    public FileInfo uploadFile(MultipartFile multipartFile) {
        if (multipartFile == null) {
            return null;
        }
        OSS ossClient = null;
        try {
            StorageProperties.OssConfig ossProperties = storageProperties.getAliyunOss();
            // 生成唯一文件名
            String contentType = multipartFile.getContentType();
            Long fileId = IdUtil.getSnowflakeNextId();
            String originalFilename = multipartFile.getOriginalFilename();
            String fileKey = ossProperties.getPrefixPath() + fileId + "_" + originalFilename;

            // 计算文件大小 单位：B
            long size = multipartFile.getSize();

            // 构造上传请求
            PutObjectRequest request = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    fileKey,
                    multipartFile.getInputStream()
            );

            // 创建OSSClient实例
            ossClient = getOSSClient();

            // 执行上传
            PutObjectResult putObjectResult = ossClient.putObject(request);
            log.info("文件上传成功, fileKey: {}, etag: {}", fileKey, putObjectResult.getETag());

            // 返回访问URL
            //String url = "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + fileKey;
            // 获取预览URL
            String presignedUrl = getPresignedUrl(fileKey);
            return FileInfo.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .size(size)
                    .type(contentType)
                    .path(fileKey)
                    .url(presignedUrl)
                    .build();
        } catch (Exception e) {
            log.error("Failed to uploadFile.", e);
            throw new BizException("文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
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
        OSS ossClient = null;
        try {
            StorageProperties.OssConfig ossProperties = storageProperties.getAliyunOss();
            // 生成唯一文件名
            String contentType = "";
            Long fileId = IdUtil.getSnowflakeNextId();
            String originalFilename = StringUtils.substringBefore(file.getName(), "?");;
            String fileKey = ossProperties.getPrefixPath() + fileId + "_" + originalFilename;

            // 计算文件大小 单位：B
            long size = file.getTotalSpace();

            // 构造上传请求
            PutObjectRequest request = new PutObjectRequest(
                    ossProperties.getBucketName(),
                    fileKey,
                    file
            );

            // 创建OSSClient实例
            ossClient = getOSSClient();

            // 执行上传
            PutObjectResult putObjectResult = ossClient.putObject(request);
            log.info("本地文件上传成功, fileKey: {}, etag: {}", fileKey, putObjectResult.getETag());

            // 返回访问URL
            //String url = "https://" + ossProperties.getBucketName() + "." + ossProperties.getEndpoint() + "/" + fileKey;
            // 获取预览URL
            String presignedUrl = getPresignedUrl(fileKey);
            return FileInfo.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .size(size)
                    .type(contentType)
                    .path(fileKey)
                    .url(presignedUrl)
                    .build();
        } catch (Exception e) {
            log.error("Failed to uploadFile.", e);
            throw new BizException("本地文件上传失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    /**
     * 下载文件
     *
     * @param fileKey 在OSS上存储的文件名(如:dwa/log.log)
     */
    @Override
    public void downloadFile(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return;
        }
        OSS ossClient = null;
        try {
            StorageProperties.OssConfig ossProperties = storageProperties.getAliyunOss();
            // ossObject包含文件所在的存储空间名称、文件名称、文件元信息以及一个输入流。
            ossClient = getOSSClient();
            OSSObject ossObject = ossClient.getObject(ossProperties.getBucketName(), fileKey);
            HttpServletResponse response = getResponse();
            try (InputStream inputStream = ossObject.getObjectContent()) {

                response.setContentType("application/octet-stream");
                response.setHeader("Content-Disposition", "attachment; filename="
                        + StringUtils.removeStart(fileKey, ossProperties.getPrefixPath()));

                byte[] buffer = new byte[1024];
                int bytesRead;
                while ((bytesRead = inputStream.read(buffer)) != -1) {
                    response.getOutputStream().write(buffer, 0, bytesRead);
                }
            } catch (Exception e) {
                log.error("Error streaming file", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            }

        } catch (Exception e) {
            log.error("Failed to downloadFile. fileKey:{}", fileKey, e);
            throw new BizException("文件下载失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    /**
     * 获取响应
     */
    private HttpServletResponse getResponse() {
        return ((ServletRequestAttributes) RequestContextHolder.currentRequestAttributes()).getResponse();
    }

    /**
     * 获取文件预签名URL
     *
     * @param fileKey 文件路径
     * @return 文件预签名URL: http://abc-oss-bucket.oss-cn-beijing.aliyuncs.com/dwa/1952770676166279188/dog.png?Expires=1755016584&OSSAccessKeyId=LTAI5tAx1r&Signature=xP6j36U5Xa
     */
    @Override
    public String getPresignedUrl(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return "";
        }
        OSS ossClient = null;
        try {
            ossClient = getOSSClient();
            StorageProperties.OssConfig ossProperties = storageProperties.getAliyunOss();
            // 过期时间
            Date expiresDate = new Date(System.currentTimeMillis() + ossProperties.getUrlExpires() * 1000L);

            // 生成1小时有效的预签名URL
            return ossClient.generatePresignedUrl(
                    ossProperties.getBucketName(),
                    fileKey,
                    expiresDate
            ).toString();
        } catch (Exception e) {
            log.error("Failed to getPresignedUrl. fileKey:{}", fileKey, e);
            throw new BizException("获取文件预签名URL失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
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
        OSS ossClient = null;
        try {
            ossClient = getOSSClient();
            String bucketName = storageProperties.getAliyunOss().getBucketName();
            VoidResult result = ossClient.deleteObject(bucketName, fileKey);
            log.info("deleteObject result:{}", JSON.toJSONString(result));
            return true;
        } catch (Exception e) {
            log.error("Failed to deleteFile. fileKey:{}", fileKey, e);
            throw new BizException("删除文件失败");
        } finally {
            if (ossClient != null) {
                ossClient.shutdown();
            }
        }
    }


    // 获取OSSClient实例
    private OSS getOSSClient() {
        StorageProperties.OssConfig ossProperties = storageProperties.getAliyunOss();
        return new OSSClientBuilder().build(
                ossProperties.getEndpoint(),
                ossProperties.getAccessKey(),
                ossProperties.getSecretKey()
        );
    }


}
