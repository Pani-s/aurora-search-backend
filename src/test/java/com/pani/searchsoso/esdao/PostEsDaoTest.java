package com.pani.searchsoso.esdao;

import com.pani.searchsoso.constant.CommonConstant;
import com.pani.searchsoso.model.dto.post.PostEsDTO;
import com.pani.searchsoso.model.dto.post.PostQueryRequest;
import com.pani.searchsoso.model.entity.Post;
import com.pani.searchsoso.service.PostService;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import javax.annotation.Resource;

import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;

/**
 * 帖子 ES 操作测试
 *
 * @author Pani
 * 
 */
@SpringBootTest
public class PostEsDaoTest {

    @Resource
    private PostEsDao postEsDao;

    @Resource
    private PostService postService;



    @Test
    void test() {
        PostQueryRequest postQueryRequest = new PostQueryRequest();
        com.baomidou.mybatisplus.extension.plugins.pagination.Page<Post> page =
                postService.searchFromEs(postQueryRequest);
        System.out.println(page);
    }

    @Test
    void testSelect() {
        System.out.println(postEsDao.count());
        Page<PostEsDTO> PostPage = postEsDao.findAll(
                PageRequest.of(0, 5, Sort.by("createTime")));
        List<PostEsDTO> postList = PostPage.getContent();
        System.out.println(postList);
    }

    @Test
    void testFindByTitle() {
        List<PostEsDTO> postEsDTOS = postEsDao.findByTitle("JDK");
        System.out.println(postEsDTOS);
    }

    @Test
    void testAdd() {
        PostEsDTO postEsDTO = new PostEsDTO();
        postEsDTO.setId(1L);
        postEsDTO.setTitle("Java");
        postEsDTO.setContent("咖啡");
        postEsDTO.setTags(Arrays.asList("java", "fun"));
        postEsDTO.setUserId(1L);
        postEsDTO.setCreateTime(new Date());
        postEsDTO.setUpdateTime(new Date());
        postEsDTO.setIsDelete(0);
        postEsDao.save(postEsDTO);
        System.out.println(postEsDTO.getId());
    }

    @Test
    void testFindById() {
        Optional<PostEsDTO> postEsDTO = postEsDao.findById(1L);
        System.out.println(postEsDTO);
    }

    @Test
    void testCount() {
        System.out.println(postEsDao.count());
    }

    @Test
    void testFindByCategory() {
        List<PostEsDTO> postEsDaoTestList = postEsDao.findByUserId(1L);
        System.out.println(postEsDaoTestList);
    }


}
