package com.dw.admin.test.storage;

import com.alibaba.fastjson2.JSON;
import com.dw.admin.components.storage.FileInfo;
import com.dw.admin.components.storage.FileStorageService;
import jakarta.annotation.Resource;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * @author dawei
 */
@SpringBootTest
@ActiveProfiles("dev")
public class FileStorageServiceTest {

    @Resource
    private FileStorageService fileStorageService;

    /**
     * 测试上传本地文件
     */
    @Test
    public void testUploadLocalFile() throws IOException {
        // 获取 resources 下面的文件
        ClassPathResource resource = new ClassPathResource("temp/cangshu-1.png");
        File file = resource.getFile();
        FileInfo fileInfo = fileStorageService.uploadFile(file);
        System.out.println("上传成功 = " + JSON.toJSONString(fileInfo));

    }
}
