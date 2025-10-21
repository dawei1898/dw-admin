package com.dw.admin.controller;

import com.dw.admin.common.entity.PageResult;
import com.dw.admin.common.entity.Response;
import com.dw.admin.components.auth.Auth;
import com.dw.admin.components.auth.UserContextHolder;
import com.dw.admin.components.log.Log;
import com.dw.admin.model.param.FilePageParam;
import com.dw.admin.model.vo.FileVo;
import com.dw.admin.service.FileService;
import jakarta.annotation.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

/**
 * <p>
 * 文件信息表 前端控制器
 * </p>
 *
 * @author dawei
 */
@RestController
@RequestMapping("/file")
public class FileController {

    @Resource
    private FileService fileServiceImpl;

    /**
     * 上传文件
     */
    @Auth
    @PostMapping("/upload")
    public Response<FileVo> uploadFile(@RequestParam(value = "file") MultipartFile file){
        Long userId = UserContextHolder.getUserId();
        FileVo fileVo = fileServiceImpl.uploadFile(file, userId);
        return Response.success(fileVo);
    }

    /**
     * 下载文件
     */
    @Auth
    @GetMapping("/download/{fileId}")
    public Response<Void> downloadFile(@PathVariable String fileId){
        fileServiceImpl.downloadFile(Long.valueOf(fileId));
        return Response.success();
    }

    /**
     * 删除文件
     */
    @Log
    @Auth
    @DeleteMapping("/delete/{fileId}")
    public Response<Boolean> deleteFile(@PathVariable String fileId){
        boolean deleted = fileServiceImpl.deleteFile(Long.valueOf(fileId));
        return Response.success(deleted);
    }

    /**
     * 获取文件信息
     */
    @Log
    @Auth
    @GetMapping("/{fileId}")
    public Response<FileVo> queryFileInfo(@PathVariable String fileId){
        FileVo fileVo = fileServiceImpl.queryFileInfo(Long.valueOf(fileId));
        return Response.success(fileVo);
    }

    /**
     * 获取图片
     */
    @GetMapping("/images/{filename:.+}")
    public ResponseEntity<UrlResource> getImage(@PathVariable String filename) {
        return fileServiceImpl.getImage(filename);
    }

    /**
     * 获取文件列表
     */
    @Log
    @Auth
    @PostMapping("/list")
    public Response<PageResult<FileVo>> queryFilePage(@RequestBody FilePageParam param) {
        PageResult<FileVo> pageResult = fileServiceImpl.queryFilePage(param);
        return Response.success(pageResult);
    }

}
