package com.pani.searchsoso.manager;

import com.pani.searchsoso.datasource.*;
import org.apache.commons.lang3.StringUtils;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.common.ErrorCode;
import com.pani.searchsoso.exception.BusinessException;
import com.pani.searchsoso.exception.ThrowUtils;
import com.pani.searchsoso.model.dto.post.PostQueryRequest;
import com.pani.searchsoso.model.dto.search.SearchRequest;

import com.pani.searchsoso.model.entity.Picture;
import com.pani.searchsoso.model.enums.SearchTypeEnum;
import com.pani.searchsoso.model.vo.PostVO;
import com.pani.searchsoso.model.vo.SearchVO;
import com.pani.searchsoso.model.vo.UserVO;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestBody;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.CompletableFuture;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/11 16:08
 * {@code @description}搜索门面-------相关：门面模式
 */
@Slf4j
@Component
public class SearchFacade {
    @Resource
    private DataSourceRegistry dataSourceRegistry;

    @Resource
    private PostDataSource postDataSource;

    @Resource
    private VideoDataSource videoDataSource;

    @Resource
    private UserDataSource userDataSource;

    @Resource
    private PictureDataSource pictureDataSource;

    public SearchVO searchAll(@RequestBody SearchRequest searchRequest) {
        String searchText = searchRequest.getSearchText();
        //current
        Long pageNum = searchRequest.getPageNum();
        Long pageSize = searchRequest.getPageSize();
        String type = searchRequest.getType();
        SearchTypeEnum searchTypeEnum = SearchTypeEnum.getEnumByValue(type);
        ThrowUtils.throwIf(StringUtils.isBlank(type), ErrorCode.PARAMS_ERROR);

        //type为空时，都搜索。
        if (searchTypeEnum == null) {
            CompletableFuture<Page<UserVO>> userTask = CompletableFuture.supplyAsync(() -> {
                //用户
                Page<UserVO> userVOPage = userDataSource.doSearch(searchText, pageNum, pageSize);
                return userVOPage;
            });
            CompletableFuture<Page<Picture>> picTask = CompletableFuture.supplyAsync(() -> {
                //图片
                Page<Picture> picturePage = pictureDataSource.doSearch(searchText, pageNum, pageSize);
                return picturePage;
            });

            CompletableFuture<Page<PostVO>> postTask = CompletableFuture.supplyAsync(() -> {
                //post
                Page<PostVO> postVOPage = postDataSource.doSearch(searchText, pageNum, pageSize);
                return postVOPage;
            });

            CompletableFuture.allOf(userTask, postTask, picTask).join();
            try {
                Page<UserVO> userVOPage = userTask.get();
                Page<PostVO> postVOPage = postTask.get();
                Page<Picture> picturePage = picTask.get();
                //装载load
                SearchVO searchVO = new SearchVO();
                searchVO.setPictureList(picturePage.getRecords());
                searchVO.setPostList(postVOPage.getRecords());
                searchVO.setUserList(userVOPage.getRecords());
                searchVO.setTotal(99L);
                return searchVO;
            } catch (Exception e) {
                log.error("查询异常！", e);
                throw new BusinessException(ErrorCode.SYSTEM_ERROR, "查询异常...");
            }
        } else {
            SearchVO searchVO = new SearchVO();
            DataSource<T> dataSource = dataSourceRegistry.getDataSourceByType(searchTypeEnum.getValue());
            Page<T> page = dataSource.doSearch(searchText, pageNum, pageSize);
            searchVO.setDataList(page.getRecords());
            searchVO.setTotal(page.getTotal());
            return searchVO;
        }
    }

}
