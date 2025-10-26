package com.dw.admin.model.param;

import com.dw.admin.common.entity.PageParam;
import lombok.Data;

import java.io.Serializable;


/**
 * 文件搜索入参
 *
 * @author dawei
 */
@Data
public class FilePageParam extends PageParam implements Serializable {

    /** 文件名称 */
    private String name;

    /** 文件类型 */
    private String type;

    /** 文件路径 */
    private String path;

    /**
     * 创建时间排序（asc：升序，desc：降序）
     */
    private String createTimeSort;

    /**
     * 更新时间排序（asc：升序，desc：降序）
     */
    private String updateTimeSort;




}
