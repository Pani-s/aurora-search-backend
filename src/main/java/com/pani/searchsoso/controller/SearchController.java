package com.pani.searchsoso.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.common.BaseResponse;
import com.pani.searchsoso.common.ErrorCode;
import com.pani.searchsoso.common.ResultUtils;
import com.pani.searchsoso.exception.BusinessException;
import com.pani.searchsoso.manager.SearchFacade;
import com.pani.searchsoso.model.dto.post.PostQueryRequest;
import com.pani.searchsoso.model.dto.search.SearchRequest;

import com.pani.searchsoso.model.vo.SearchVO;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;


/**
 * @author Pani
 * {@code @date} Created in 2023/11/9 21:42
 * {@code @description} 实现聚合搜索
 */
@RestController
@RequestMapping("/search")
@Slf4j
public class SearchController {
    @Resource
    private SearchFacade searchFacade;

    @PostMapping("/all")
    public BaseResponse<SearchVO> searchAll(@RequestBody SearchRequest searchRequest) {
        return ResultUtils.success(searchFacade.searchAll(searchRequest));

    }
}
