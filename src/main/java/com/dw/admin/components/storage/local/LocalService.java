package com.dw.admin.components.storage.local;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.dw.admin.common.exception.BizException;
import com.dw.admin.common.utils.RequestHolder;
import com.dw.admin.components.storage.FileInfo;
import com.dw.admin.components.storage.FileStorageService;
import com.dw.admin.components.storage.StorageConstant;
import com.dw.admin.components.storage.StorageProperties;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;


/**
 * 本地存储服务类
 *
 * @author dawei
 */
@Slf4j
@RestController
@Component
@ConditionalOnProperty(
        name = StorageConstant.STORAGE_PROVIDER_KEY,
        havingValue = StorageConstant.PROVIDER_LOCAL
)
public class LocalService implements FileStorageService {


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
        try {
            StorageProperties.LocalConfig localConfig = storageProperties.getLocal();
            Long fileId = IdUtil.getSnowflakeNextId();
            String contentType = multipartFile.getContentType();
            String originalFilename = multipartFile.getOriginalFilename();
            String fileName = fileId + "_" + originalFilename;
            String filePath = localConfig.getPrefixPath() + fileName;
            // 计算文件大小
            long size = multipartFile.getSize();

            Path path = Path.of(filePath);
            // 复制文件保存到指定目录
            FileUtil.copyFile(multipartFile.getInputStream(), path, StandardCopyOption.REPLACE_EXISTING);

            // 获取预览URL
            String presignedUrl = getPresignedUrl(filePath);
            log.info("文件上传成功: presignedUrl:{}", presignedUrl);

            // 返回文件信息
            return FileInfo.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .size(size)
                    .type(contentType)
                    .path(filePath)
                    .url(presignedUrl)
                    .build();
        } catch (Exception e) {
            log.error("文件上传失败", e);
            throw new BizException("文件上传失败: " + e.getMessage());
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
        try {
            StorageProperties.LocalConfig localConfig = storageProperties.getLocal();
            // 生成唯一文件名
            String contentType = "";
            Long fileId = IdUtil.getSnowflakeNextId();
            String originalFilename = StringUtils.substringBefore(file.getName(), "?");;
            String fileName = fileId + "_" + originalFilename;
            String filePath = localConfig.getPrefixPath() + fileName;

            // 计算文件大小
            long size = file.getTotalSpace();

            Path path = Path.of(filePath);
            // 复制文件保存到指定目录
            FileUtil.copyFile(new FileInputStream(file), path, StandardCopyOption.REPLACE_EXISTING);

            // 获取预览URL
            String presignedUrl = getPresignedUrl(filePath);
            log.info("文件上传成功: presignedUrl:{}", presignedUrl);

            // 返回文件信息
            return FileInfo.builder()
                    .id(fileId)
                    .name(originalFilename)
                    .size(size)
                    .type(contentType)
                    .path(filePath)
                    .url(presignedUrl)
                    .build();
        } catch (Exception e) {
            log.error("本地文件上传失败", e);
            throw new BizException("本地文件上传失败: " + e.getMessage());
        }
    }


    /**
     * 下载文件
     *
     * @param filePath 文件路径 /data/dw-test/1975838072023347200_香蕉banana.png
     */
    @Override
    public void downloadFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return;
        }
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
            String fileName = URLEncoder.encode(file.getName(), StandardCharsets.UTF_8);
            // 设置响应头信息
            //response.setContentType(files.getFileType());
            response.setHeader("Content-Disposition", "attachment; filename=" + fileName);

            // 将文件流写入响应输出流
            IOUtils.copy(inputStream, response.getOutputStream());
            response.flushBuffer();
        } catch (Exception e) {
            log.error("文件下载失败: {} .", filePath, e);
            throw new BizException("文件下载失败");
        }
    }

    /**
     * 获取文件预签名URL
     *
     * @param fileKey 文件路径 /data/dw-test/1975838072023347200_香蕉banana.png
     * @return 文件预览URL: http://127.0.0.1:8020/file/preview/data/dw-test/1975838072023347200_香蕉banana.png
     */
    @Override
    public String getPresignedUrl(String fileKey) {
        if (StringUtils.isEmpty(fileKey)) {
            return "";
        }
        StorageProperties.LocalConfig localConfig = storageProperties.getLocal();
        return localConfig.getPreviewDomain() + localConfig.getPreviewPath() + fileKey;
    }


    /**
     * 删除文件
     *
     * @param filePath 文件路径
     * @return 是否删除成功
     */
    @Override
    public boolean deleteFile(String filePath) {
        if (StringUtils.isEmpty(filePath)) {
            return false;
        }

        try {
            // 删除存储文件
            File file = new File(filePath);
            if (file.exists()) {
                file.delete();
                log.info("文件删除成功: {}", filePath);
            }
        } catch (Exception e) {
            log.error("文件删除失败, filePath: {}.", filePath, e);
            return false;
        }
        return true;
    }


    /**
     * 预览文件
     */
    //@GetMapping("/file/preview/data/dw-test/{filename:.+}")
    @GetMapping("${dwa.storage.local.preview-path}${dwa.storage.local.prefix-path}{filename:.+}")
    public ResponseEntity<UrlResource> previewFile(@PathVariable String filename) {
        try {
            // filename: 1975838072023347200_香蕉banana.png
            log.info("开始预览文件: {}",  filename);
            // 解码 URL 中的文件名（前端可能 encodeURIComponent）
            String decodedFilename = URLDecoder.decode(filename, StandardCharsets.UTF_8);

            // 安全校验：防止路径遍历（如 filename=../../../etc/passwd）
            if (decodedFilename.contains("..")) {
                return ResponseEntity.badRequest().build();
            }

            // data/dwa-test/
            Path uploadPath = Paths.get(storageProperties.getLocal().getPrefixPath()).normalize();
            // data/dwa-test/1975838072023347200_香蕉banana.png
            Path filePath = uploadPath.resolve(decodedFilename).normalize();

            // 确保文件在 uploadDir 目录内
            if (!filePath.startsWith(uploadPath)) {
                return ResponseEntity.badRequest().build();
            }

            UrlResource resource = new UrlResource(filePath.toUri());
            if (resource.exists() && resource.isReadable()) {
                // 设置 Content-Type 自动识别
                String contentType = Files.probeContentType(filePath);
                if (contentType == null) {
                    contentType = "application/octet-stream";
                }

                // 图片名称
                ContentDisposition contentDisposition = ContentDisposition.inline()
                        .filename(resource.getFilename(), StandardCharsets.UTF_8)
                        .build();

                log.info("预览文件成功: {}",  filename);
                // 返回文件资源
                return ResponseEntity.ok()
                        .contentType(org.springframework.http.MediaType.parseMediaType(contentType))
                        .headers(httpHeaders -> httpHeaders.setContentDisposition(contentDisposition))
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            log.error("预览文件失败: {} .",  filename, e);
            return ResponseEntity.status(500).build();
        }
    }

}