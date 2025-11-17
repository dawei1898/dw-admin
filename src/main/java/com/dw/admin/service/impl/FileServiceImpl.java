package com.dw.admin.service.impl;


import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.enums.SortEnum;
import com.dw.admin.components.storage.StorageUrlUtil;
import com.dw.admin.common.utils.ValidateUtil;
import com.dw.admin.components.storage.FileInfo;
import com.dw.admin.components.storage.FileStorageService;
import com.dw.admin.dao.FileMapper;

import com.dw.admin.model.entity.DwaFile;
import com.dw.admin.model.param.FilePageParam;
import com.dw.admin.model.vo.FileVo;

import com.dw.admin.service.FileService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * 文件服务实现类
 *
 * @author dawei
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    @Resource
    private FileStorageService fileStorageService;

    @Resource
    private FileMapper fileMapper;

    /**
     * 上传文件
     */
    @Override
    public FileInfo uploadFile(MultipartFile file, Long userId) {
        ValidateUtil.isNull(file, "文件内容不能为空！");
        FileInfo fileInfo = fileStorageService.uploadFile(file);
        if (fileInfo == null) {
            return fileInfo;
        }
       /* if (StringUtils.isNotBlank(fileInfo.getPath())) {
            //  有权限的预览 URL
            String presignedUrl = fileStorageService.getPresignedUrl(fileInfo.getPath());
            fileInfo.setUrl(presignedUrl);
        }*/

        // 过期时间
        Integer expiresTime = StorageUrlUtil.getExpiresValue(fileInfo.getUrl());

        // 保存文件信息
        DwaFile dwaFile = DwaFile.builder()
                .fileId(fileInfo.getId())
                .fileName(fileInfo.getName())
                .fileType(fileInfo.getType())
                .fileSize(fileInfo.getSize())
                .filePath(fileInfo.getPath())
                .fileUrl(fileInfo.getUrl())
                .urlExpires(expiresTime)
                .createUser(userId)
                .updateUser(userId)
                .build();
        fileMapper.insert(dwaFile);

        return fileInfo;
    }


    /**
     * 下载文件
     */
    @Override
    public void downloadFile(Long fileId) {
        ValidateUtil.isNull(fileId, "fileId不能为空！");

        DwaFile dwaFile = fileMapper.selectById(fileId);
        ValidateUtil.isNull(dwaFile, "文件不存在！");

        if (StringUtils.isNotBlank(dwaFile.getFilePath())) {
            fileStorageService.downloadFile(dwaFile.getFilePath());
        }
        log.info("文件下载成功: {}", dwaFile.getFilePath());
    }

    /**
     * 删除文件
     */
    @Override
    public boolean deleteFile(Long fileId) {
        ValidateUtil.isNull(fileId, "fileId不能为空！");
        try {
            DwaFile exist = fileMapper.selectById(fileId);
            if (exist != null) {
                // 删除存储文件
                fileStorageService.deleteFile(exist.getFilePath());
                // 删除文件信息
                int i = fileMapper.deleteById(fileId);
                if (i > 0) {
                    return true;
                }
            }
        } catch (Exception e) {
            log.error("文件删除失败,fileId: {}.", fileId, e);
        }
        return false;
    }

    /**
     * 获取文件预签名URL
     *
     * @param fileKey  文件路径
     * @return 文件预签名URL
     */
    public String getPresignedUrl(String fileKey) {
        return fileStorageService.getPresignedUrl(fileKey);
    }


    /**
     * 获取文件信息
     */
    @Override
    public FileVo queryFileInfo(Long fileId) {
        ValidateUtil.isNull(fileId, "fileId不能为空！");
        DwaFile file = fileMapper.selectById(fileId);
        if (file == null) {
            return null;
        }

        // url 如果过期，则刷新url
        refreshUrl(file);

        return dwaFile2FileVo(file);
    }


    /**
     *  如果 url 过期，则刷新
     */
    private void refreshUrl(DwaFile dwaFile) {
        if (dwaFile == null || StringUtils.isEmpty(dwaFile.getFileUrl())) {
            log.warn("File is null or url is empty.");
            return;
        }
        try {
            if (StorageUrlUtil.isExpired(dwaFile.getFileUrl())) {
                String presignedUrl = this.getPresignedUrl(dwaFile.getFilePath());
                if (StringUtils.isNotEmpty(presignedUrl)) {
                    dwaFile.setFileUrl(presignedUrl);
                    // 刷新库中的 url
                    DwaFile update = new DwaFile();
                    update.setFileId(dwaFile.getFileId());
                    update.setFileUrl(presignedUrl);
                    update.setUrlExpires(StorageUrlUtil.getExpiresValue(presignedUrl));
                    fileMapper.updateById(dwaFile);
                }
            }
        } catch (Exception e) {
            log.error("Failed to refreshUrl.", e);
        }
    }

    /**
     * 获取文件列表
     */
    @Override
    public PageResult<FileVo> queryFilePage(FilePageParam param) {
        ValidateUtil.isNull(param, "参数不能为空!");
        LambdaQueryWrapper<DwaFile> queryWrapper = new LambdaQueryWrapper<>();
        // 文件名称模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getName()),
                DwaFile::getFileName, param.getName());
        // 文件类型模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getType()),
                    DwaFile::getFileType, param.getType());
        // 文件路径模糊搜索
        queryWrapper.like(StringUtils.isNotBlank(param.getPath()),
                    DwaFile::getFilePath, param.getPath());
        // 默认排序：创建时间降序
        if (StringUtils.isAllEmpty(param.getCreateTimeSort(), param.getUpdateTimeSort())) {
            queryWrapper.orderByDesc(DwaFile::getCreateTime);
        } else {
            // 创建时间排序
            if (SortEnum.ASC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByAsc(DwaFile::getCreateTime);
            } else if (SortEnum.DESC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByDesc(DwaFile::getCreateTime);
            }
            // 更新时间排序
            if (SortEnum.ASC.getCode().equalsIgnoreCase(param.getUpdateTimeSort())) {
                queryWrapper.orderByAsc(DwaFile::getUpdateTime);
            } else if (SortEnum.DESC.getCode().equalsIgnoreCase(param.getUpdateTimeSort())) {
                queryWrapper.orderByDesc(DwaFile::getUpdateTime);
            }
        }
        // 分页查询
        Page<DwaFile> page = new Page<>(param.getPageNum(), param.getPageSize());
        fileMapper.selectPage(page, queryWrapper);
        // 封装结果
        List<FileVo> fileVos = page.getRecords().stream().map(this::dwaFile2FileVo).toList();
        return PageResult.build(param.getPageNum(), param.getPageSize(), page.getTotal(), fileVos);
    }


    private FileVo dwaFile2FileVo(DwaFile file) {
        if (file == null) {
            return null;
        }
        return FileVo.builder()
                .id(file.getFileId())
                .name(file.getFileName())
                .type(file.getFileType())
                .size(file.getFileSize())
                .path(file.getFilePath())
                .url(file.getFileUrl())
                .createUser(file.getCreateUser())
                .updateUser(file.getCreateUser())
                .createTime(file.getCreateTime())
                .updateTime(file.getUpdateTime())
                .build();
    }

}
