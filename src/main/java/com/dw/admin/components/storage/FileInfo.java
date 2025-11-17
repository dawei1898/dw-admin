package com.dw.admin.components.storage;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;

/**
 * 上传文件返回的信息
 *
 * @author dawei
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FileInfo implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;

    /** 文件 ID */
    private Long id;

    /** 文件名称 */
    private String name;

    /** 文件类型（text/plain、image/png、audio/mpeg、video/mp4） */
    private String type;

    /** 文件大小（ B ） */
    private Long size;

    /** 文件路径 */
    private String path;

    /** 文件URL */
    private String url;

}
