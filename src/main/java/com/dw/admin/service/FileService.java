package com.dw.admin.service;

import com.dw.admin.common.entity.PageResult;
import com.dw.admin.components.oss.FileInfo;
import com.dw.admin.model.param.FilePageParam;
import com.dw.admin.model.vo.FileVo;
import org.springframework.web.multipart.MultipartFile;

/**
 * 文件服务 服务类
 *
 * @author dawei
 */
public interface FileService {

    /**
     * 上传文件
     */
    FileInfo uploadFile(MultipartFile file, Long userId);

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
     * 获取文件列表
     */
    PageResult<FileVo> queryFilePage(FilePageParam param);

}
