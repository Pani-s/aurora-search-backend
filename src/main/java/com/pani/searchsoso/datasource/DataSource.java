package com.pani.searchsoso.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/11 17:05
 * {@code @description}数据源接口
 */
public interface DataSource<T> {
    /**
     * 搜索，统一接口
     * @param searchText
     * @param pageNum
     * @param pageSize
     * @return
     */
    Page<T> doSearch(String searchText, long pageNum, long pageSize);
}
