package com.pani.searchsoso.common;

import com.pani.searchsoso.constant.CommonConstant;
import lombok.Data;

/**
 * 分页请求
 *
 * @author Pani
 * 
 */
@Data
public class PageRequest {

    /**
     * 当前页号
     */
    private long pageNum = 1L;

    /**
     * 页面大小
     */
    private long pageSize = 10L;

    /**
     * 排序字段
     */
    private String sortField;

    /**
     * 排序顺序（默认升序）
     */
    private String sortOrder = CommonConstant.SORT_ORDER_ASC;
}
