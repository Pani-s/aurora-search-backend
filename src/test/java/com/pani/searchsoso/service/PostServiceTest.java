package com.pani.searchsoso.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.model.dto.post.PostQueryRequest;
import com.pani.searchsoso.model.entity.Post;

import javax.annotation.Resource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * 帖子服务测试
 *
 * @author Pani
 * 
 */
@SpringBootTest
class PostServiceTest {

    @Resource
    private PostService postService;

    @Test
    void searchFromEs() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        postQueryRequest.setSearchText("JDK");
        Page<Post> postPage = postService.searchFromEs(postQueryRequest);
        System.out.println(postPage.getRecords());
        Assertions.assertNotNull(postPage);
    }

}