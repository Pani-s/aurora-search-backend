package com.pani.searchsoso.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.model.dto.post.PostQueryRequest;

import com.pani.searchsoso.model.dto.user.UserQueryRequest;
import com.pani.searchsoso.model.entity.Post;
import com.pani.searchsoso.model.vo.PostVO;
import com.pani.searchsoso.service.PostService;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/11 17:08
 * {@code @description}帖子文章数据源处理
 */
@Service
public class PostDataSource implements DataSource<PostVO> {

    @Resource
    private PostService postService;

    //    @Override
    //    public Page<PostVO> doSearch(String searchText, long pageNum, long pageSize) {
    //        PostQueryRequest postQueryRequest = new PostQueryRequest();
    //        postQueryRequest.setSearchText(searchText);
    //        postQueryRequest.setPageNum(pageNum);
    //        postQueryRequest.setPageSize(pageSize);
    //        //获取http request
    //        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
    //        HttpServletRequest request = servletRequestAttributes.getRequest();
    //        return postService.listPostVOByPage(postQueryRequest, request);
    //    }

    @Override
    public Page<PostVO> doSearch(String searchText, long pageNum, long pageSize) {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        //http request
        ServletRequestAttributes servletRequestAttributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = servletRequestAttributes.getRequest();
        postQueryRequest.setSearchText(searchText);
        postQueryRequest.setPageNum(pageNum);
        postQueryRequest.setPageSize(pageSize);
        if (searchText == null) {
            return postService.listPostVOByPage(postQueryRequest, request);
        }
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        return postService.getPostVOPage(postPage, request);
    }

}
