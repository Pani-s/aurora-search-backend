package com.pani.searchsoso.model.dto.picture;

import com.pani.searchsoso.common.PageRequest;
import lombok.Data;

import java.io.Serializable;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/9 20:09
 * {@code @description}
 */
@Data
public class PictureQueryRequest extends PageRequest implements Serializable {
    /**
     * 从哪里开始
     */
    private long pageNum;
    /**
     * 页面大小
     */
    private long pageSize;
    /**
     * 搜索词
     */
    private String searchText;
}
