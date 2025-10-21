package com.dw.admin.service.impl;


import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.enums.SortEnum;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.common.utils.RequestHolder;
import com.dw.admin.common.utils.ValidateUtil;
import com.dw.admin.dao.FileMapper;

import com.dw.admin.model.entity.DwaFile;
import com.dw.admin.model.param.FilePageParam;
import com.dw.admin.model.vo.FileVo;

import com.dw.admin.service.FileService;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * 文件信息表 服务实现类
 *
 * @author dawei
 */
@Slf4j
@Service
public class FileServiceImpl implements FileService {

    // 文件保存路径前缀
    public static final String FILE_PATH = "data/";

    @Value("${server.port}")
    private int port;

    @Resource
    private FileMapper fileMapper;

    /**
     * 上传文件
     */
    @Override
    public FileVo uploadFile(MultipartFile file, Long userId) {
        ValidateUtil.isNull(file, "文件内容不能为空！");
        Long fileId = IdUtil.getSnowflakeNextId();
        String fileContentType = file.getContentType();
        String originalFilename = file.getOriginalFilename();
        String fileName = fileId + "_" + originalFilename;
        String filePath = FILE_PATH + fileName;

        try {
            Path path = Path.of(filePath);
            // 复制文件保存到指定目录
            FileUtil.copyFile(file.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            log.error("Failed to copyFile to path: {}", filePath, e);
            throw new BizException("文件保存失败: " + e.getMessage());
        }

        // 文件存储的完整 URL
        String fileUrl = getFileUrl(filePath, fileName, fileContentType);

        DwaFile files = DwaFile.builder()
                .fileId(fileId)
                .fileName(originalFilename)
                .fileType(fileContentType)
                .filePath(filePath)
                .fileUrl(fileUrl)
                .createUser(userId)
                .build();
        fileMapper.insert(files);
        return BeanUtil.copyProperties(files, FileVo.class);
    }

    private String getFileUrl(String filePath, String fileName, String fileContentType) {
        String fileUrl = null;
        try {
            // 获取本机 IP 地址
            String ip = RequestHolder.getHttpServletRequestIpAddress();
            String serverBaseUrl = "http://" + ip + ":" + port;
            /*HttpServletRequest request = RequestHolder.getHttpServletRequest();
            StringBuffer requestUrl = request.getRequestURL();
            String serverBaseUrl = requestUrl.substring(0, requestUrl.indexOf(request.getServletPath()));*/
            if (StrUtil.startWith(fileContentType, "image")) {
                // 图片  http://localhost:8010/file/images/1975838072023347200_香蕉banana.png
                fileUrl = serverBaseUrl + "/file/images/" + fileName;
            } else {
                // 其他文件  http://localhost:8010/data/1975838072023347211_banana.txt
                fileUrl = serverBaseUrl + "/" + filePath;
            }

        } catch (Exception e) {
            log.error("Failed to get fileUrl.", e);
            throw new BizException(e);
        }
        return fileUrl;
    }

    /**
     * 下载文件
     */
    @Override
    public void downloadFile(Long fileId) {
        ValidateUtil.isNull(fileId, "fileId不能为空！");

        DwaFile dwaFile = fileMapper.selectById(fileId);
        ValidateUtil.isNull(dwaFile, "文件不存在！");

        String filePath = dwaFile.getFilePath();
        Path path = Path.of(filePath);
        File file = path.toFile();
        // 检查文件是否真实存在
        if (!file.exists()) {
            throw new BizException("文件不存在或已被删除");
        }

        // 获取 HttpServletResponse 对象
        HttpServletResponse response = RequestHolder.getHttpServletResponse();

        // InputStream inputStream = new FileInputStream(file);
        try (InputStream inputStream = FileUtil.getInputStream(file)) {
            String fileName = URLEncoder.encode(dwaFile.getFileName(), StandardCharsets.UTF_8);
            // 设置响应头信息
            response.setContentType(dwaFile.getFileType());
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 将文件流写入响应输出流
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("文件下载失败: {} .", filePath, e);
            throw new BizException("文件下载失败");
        }
        log.info("文件下载成功: {}", filePath);
    }

    /**
     * 删除文件
     */
    @Override
    public boolean deleteFile(Long fileId) {
        ValidateUtil.isNull(fileId, "fileId不能为空！");
        try {
            // 删除文件信息
            DwaFile dwaFile = fileMapper.selectById(fileId);
            if (dwaFile != null) {
                fileMapper.deleteById(fileId);
            }

            // 删除存储文件
            if (dwaFile != null) {
                String filePath = dwaFile.getFilePath();
                File file = new File(filePath);
                if (file.exists()) {
                    file.delete();
                    log.info("文件删除成功: {}", filePath);
                }
            }
        } catch (Exception e) {
            log.error("文件删除失败,fileId: {}.", fileId, e);
            return false;
        }
        return true;
    }


    /**
     * 获取文件信息
     */
    @Override
    public FileVo queryFileInfo(Long fileId) {
        ValidateUtil.isNull(fileId, "fileId不能为空！");
        DwaFile file = fileMapper.selectById(fileId);
        return BeanUtil.copyProperties(file, FileVo.class);
    }

    /**
     * 获取图片
     */
    public ResponseEntity<UrlResource> getImage(String filename) {
        try {
            log.info("开始获取图片: {}",  filename);
            // 解码 URL 中的文件名（前端可能 encodeURIComponent）
            String decodedFilename = java.net.URLDecoder.decode(filename, StandardCharsets.UTF_8);

            // 安全校验：防止路径遍历（如 filename=../../../etc/passwd）
            if (decodedFilename.contains("..")) {
                return ResponseEntity.badRequest().build();
            }

            Path uploadPath = Paths.get(FILE_PATH).normalize();
            // data/1975838072023347200_香蕉banana.png
            Path filePath = uploadPath.resolve(decodedFilename).normalize();

            // 确保文件在 uploadDir 目录内
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            UrlResource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // 设置 Content-Type 自动识别
                String contentType = java.nio.file.Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                // 图片名称
                ContentDisposition contentDisposition = ContentDisposition.inline()
                        .filename(resource.getFilename(), StandardCharsets.UTF_8)
                        .build();

                log.info("获取图片成功: {}",  filename);
                // 返回图片资源
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .headers(httpHeaders -> httpHeaders.setContentDisposition(contentDisposition))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("获取图片失败: {} .",  filename, e);
            return ResponseEntity.status(500).build();
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
        queryWrapper.like(StrUtil.isNotBlank(param.getFileName()),
                DwaFile::getFileName, param.getFileName());
        // 默认排序：创建时间降序
        if (StrUtil.isEmpty(param.getCreateTimeSort())) {
            queryWrapper.orderByDesc(DwaFile::getCreateTime);
        } else {
            // 创建时间排序
            if (SortEnum.ASC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByAsc(DwaFile::getCreateTime);
            } else if (SortEnum.DESC.getCode().equalsIgnoreCase(param.getCreateTimeSort())) {
                queryWrapper.orderByDesc(DwaFile::getCreateTime);
            }
        }
        // 分页查询
        Page<DwaFile> page = new Page<>(param.getPageNum(), param.getPageSize());
        fileMapper.selectPage(page, queryWrapper);
        // 封装结果
        List<FileVo> fileVos = BeanUtil.copyToList(page.getRecords(), FileVo.class);
        return PageResult.build(param.getPageNum(), param.getPageSize(), page.getTotal(), fileVos);
    }


}
