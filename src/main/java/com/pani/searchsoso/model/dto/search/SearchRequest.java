package com.pani.searchsoso.model.dto.search;

import com.pani.searchsoso.common.PageRequest;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/9 22:14
 * {@code @description} 查询请求
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class SearchRequest extends PageRequest implements Serializable {
    /**
     * 搜索词
     */
    private String searchText;
    /**
     * 类型
     */
    private String type;

    private static final long serialVersionUID = 1L;

}
