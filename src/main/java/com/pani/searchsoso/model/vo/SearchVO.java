package com.pani.searchsoso.model.vo;

import com.pani.searchsoso.model.entity.Picture;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/9 21:58
 * {@code @description}
 */
@Data
public class SearchVO implements Serializable {
    private List<UserVO> userList;
    private List<PostVO> postList;
    private List<Picture> pictureList;
    private List<?> dataList;
    private Long total;
    private static final long serialVersionUID = 1L;

}
