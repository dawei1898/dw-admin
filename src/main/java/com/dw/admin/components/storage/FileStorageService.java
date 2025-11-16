package com.dw.admin.components.storage;

import com.dw.admin.components.oss.FileInfo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 统一文件存储服务接口
 *
 * @author dw-admin
 */
public interface FileStorageService {

    /**
     * 上传文件
     *
     * @param multipartFile 文件信息
     * @return 文件存储信息
     */
    FileInfo uploadFile(MultipartFile multipartFile);

    /**
     * 下载文件
     *
     * @param fileKey 文件路径
     */
    void downloadFile(String fileKey);

    /**
     * 获取预签名URL
     *
     * @param fileKey 文件路径
     * @return 文件预签名URL
     */
    String getPresignedUrl(String fileKey);

    /**
     * 删除文件
     *
     * @param fileKey 文件路径
     * @return 是否删除成功
     */
    boolean deleteFile(String fileKey);
}