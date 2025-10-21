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

    /**
     * 文件名称
     */
    private String fileName;


    /**
     * 创建时间排序（asc：升序，desc：降序）
     */
    private String createTimeSort;


}
