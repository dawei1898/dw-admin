package com.dw.admin.service;

import com.dw.admin.common.entity.PageResult;
import com.dw.admin.model.param.FilePageParam;
import com.dw.admin.model.vo.FileVo;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件信息表 服务类
 * </p>
 *
 * @author dawei
 */
public interface FileService {

    /**
     * 上传文件
     */
    FileVo uploadFile(MultipartFile file, Long userId);


    /**
     * 下载文件
     */
    void downloadFile(Long fileId);

    /**
     * 删除文件
     */
    boolean deleteFile(Long fileId);

    /**
     * 获取文件信息
     */
    FileVo queryFileInfo(Long fileId);

    /**
     * 获取图片
     */
    ResponseEntity<UrlResource> getImage(String filename);


    /**
     * 获取文件列表
     */
    PageResult<FileVo> queryFilePage(FilePageParam param);

}
