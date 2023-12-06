package com.pani.searchsoso.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.google.gson.Gson;
import com.pani.searchsoso.exception.BusinessException;
import com.pani.searchsoso.exception.ThrowUtils;
import com.pani.searchsoso.common.ErrorCode;
import com.pani.searchsoso.constant.CommonConstant;
import com.pani.searchsoso.mapper.PostFavourMapper;
import com.pani.searchsoso.mapper.PostMapper;
import com.pani.searchsoso.mapper.PostThumbMapper;
import com.pani.searchsoso.model.dto.post.PostEsDTO;
import com.pani.searchsoso.model.dto.post.PostEsHighlightData;
import com.pani.searchsoso.model.dto.post.PostQueryRequest;
import com.pani.searchsoso.model.entity.Post;
import com.pani.searchsoso.model.entity.PostFavour;
import com.pani.searchsoso.model.entity.PostThumb;
import com.pani.searchsoso.model.entity.User;
import com.pani.searchsoso.model.vo.PostVO;
import com.pani.searchsoso.model.vo.UserVO;
import com.pani.searchsoso.service.PostService;
import com.pani.searchsoso.service.UserService;
import com.pani.searchsoso.utils.SqlUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.elasticsearch.index.query.BoolQueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.search.fetch.subphase.highlight.HighlightBuilder;
import org.elasticsearch.search.sort.SortBuilder;
import org.elasticsearch.search.sort.SortBuilders;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.ElasticsearchRestTemplate;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.NativeSearchQuery;
import org.springframework.data.elasticsearch.core.query.NativeSearchQueryBuilder;
import org.springframework.stereotype.Service;

/**
 * 帖子服务实现
 *
 * @author Pani
 */
@Service
@Slf4j
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements PostService {

    private final static Gson GSON = new Gson();

    @Resource
    private UserService userService;

    @Resource
    private PostThumbMapper postThumbMapper;

    @Resource
    private PostFavourMapper postFavourMapper;

    @Resource
    private ElasticsearchRestTemplate elasticsearchRestTemplate;

    @Override
    public void validPost(Post post, boolean add) {
        if (post == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        String title = post.getTitle();
        String content = post.getContent();
        String tags = post.getTags();
        // 创建时，参数不能为空
        if (add) {
            ThrowUtils.throwIf(StringUtils.isAnyBlank(title, content, tags), ErrorCode.PARAMS_ERROR);
        }
        // 有参数则校验
        if (StringUtils.isNotBlank(title) && title.length() > 80) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标题过长");
        }
        if (StringUtils.isNotBlank(content) && content.length() > 8192) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "内容过长");
        }
    }

    /**
     * 获取查询包装类
     *
     * @param postQueryRequest
     * @return
     */
    @Override
    public QueryWrapper<Post> getQueryWrapper(PostQueryRequest postQueryRequest) {
        QueryWrapper<Post> queryWrapper = new QueryWrapper<>();
        if (postQueryRequest == null) {
            return queryWrapper;
        }
        String searchText = postQueryRequest.getSearchText();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        Long id = postQueryRequest.getId();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        Long userId = postQueryRequest.getUserId();
        Long notId = postQueryRequest.getNotId();
        // 拼接查询条件
        //关键词搜索，标题和内容,null空情况
        if (StringUtils.isNotBlank(searchText)) {
            queryWrapper.like("title", searchText).or().like("content", searchText);
        }
        queryWrapper.like(StringUtils.isNotBlank(title), "title", title);
        queryWrapper.like(StringUtils.isNotBlank(content), "content", content);
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                queryWrapper.like("tags", "\"" + tag + "\"");
            }
        }
        queryWrapper.ne(ObjectUtils.isNotEmpty(notId), "id", notId);
        queryWrapper.eq(ObjectUtils.isNotEmpty(id), "id", id);
        queryWrapper.eq(ObjectUtils.isNotEmpty(userId), "userId", userId);
        queryWrapper.eq("isDelete", false);
        queryWrapper.orderBy(SqlUtils.validSortField(sortField), sortOrder.equals(CommonConstant.SORT_ORDER_ASC),
                sortField);
        return queryWrapper;
    }

    @Override
    public Page<Post> searchFromEs(PostQueryRequest postQueryRequest) {
        Long id = postQueryRequest.getId();
        Long notId = postQueryRequest.getNotId();
        String searchText = postQueryRequest.getSearchText();
        String title = postQueryRequest.getTitle();
        String content = postQueryRequest.getContent();
        List<String> tagList = postQueryRequest.getTags();
        List<String> orTagList = postQueryRequest.getOrTags();
        Long userId = postQueryRequest.getUserId();
        // es 起始页为 0
        long current = postQueryRequest.getPageNum() - 1;
        long pageSize = postQueryRequest.getPageSize();
        String sortField = postQueryRequest.getSortField();
        String sortOrder = postQueryRequest.getSortOrder();
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();
        //查询带highlight，标题和内容都带上
        HighlightBuilder highlightBuilder = new HighlightBuilder()
                .field("content").requireFieldMatch(false)
                .preTags("<strong style=\"color: #8470FF\">")
                .postTags("</strong>");
        highlightBuilder.field("title").requireFieldMatch(false)
                .preTags("<strong style=\"color: #8470FF\">")
                .postTags("</strong>");
        //如果要多个字段高亮,这项要为false
        highlightBuilder.requireFieldMatch(false);
        // 过滤
        boolQueryBuilder.filter(QueryBuilders.termQuery("isDelete", 0));
        if (id != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("id", id));
        }
        if (notId != null) {
            boolQueryBuilder.mustNot(QueryBuilders.termQuery("id", notId));
        }
        if (userId != null) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("userId", userId));
        }
        //必须包含所有标签
        if (CollectionUtils.isNotEmpty(tagList)) {
            for (String tag : tagList) {
                boolQueryBuilder.filter(QueryBuilders.termQuery("tags", tag));
            }
        }

        // 包含任何一个标签即可
        if (CollectionUtils.isNotEmpty(orTagList)) {
            BoolQueryBuilder orTagBoolQueryBuilder = QueryBuilders.boolQuery();
            for (String tag : orTagList) {
                orTagBoolQueryBuilder.should(QueryBuilders.termQuery("tags", tag));
            }
            orTagBoolQueryBuilder.minimumShouldMatch(1);
            boolQueryBuilder.filter(orTagBoolQueryBuilder);
        }

        // 按关键词检索
        if (StringUtils.isNotBlank(searchText)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("description", searchText));
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", searchText));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按标题检索
        if (StringUtils.isNotBlank(title)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("title", title));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 按内容检索
        if (StringUtils.isNotBlank(content)) {
            boolQueryBuilder.should(QueryBuilders.matchQuery("content", content));
            boolQueryBuilder.minimumShouldMatch(1);
        }
        // 排序
        SortBuilder<?> sortBuilder = SortBuilders.scoreSort();
        if (StringUtils.isNotBlank(sortField)) {
            sortBuilder = SortBuilders.fieldSort(sortField);
            sortBuilder.order(CommonConstant.SORT_ORDER_ASC.equals(sortOrder) ? SortOrder.ASC : SortOrder.DESC);
        }
        // 分页
        PageRequest pageRequest = PageRequest.of((int) current, (int) pageSize);
        // 构造查询
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder().withQuery(boolQueryBuilder)
                .withPageable(pageRequest).withSorts(sortBuilder).withHighlightBuilder(highlightBuilder).build();
        SearchHits<PostEsDTO> searchHits = elasticsearchRestTemplate.search(searchQuery, PostEsDTO.class);


        //====================================
        //        // 设置高亮
        //        HashMap<Long, String> postIdContentHighLight = new HashMap<>();
        //        HashMap<Long, String> postIdTitleHighLight = new HashMap<>();
        //        //=====================
        //        searchHits.stream().forEach(searchHit -> {
        //            Long postId = searchHit.getContent().getId();
        //            List<String> highlightFields = searchHit.getHighlightField("content");
        //            if (CollectionUtils.isNotEmpty(highlightFields)) {
        //                // 每篇文章只展示匹配到的第一个高亮内容，它这个高亮部分不知道是怎么划分的，按段吗？
        //                String contentHighLight = highlightFields.get(0);
        //                if (StringUtils.isNotBlank(contentHighLight)) {
        //                    postIdContentHighLight.put(postId, contentHighLight);
        //                }
        //            }
        //            highlightFields = searchHit.getHighlightField("title");
        //            if (CollectionUtils.isNotEmpty(highlightFields)) {
        //                // 每个标题只展示匹配到的第一个高亮内容
        //                String titleHighLight = highlightFields.get(0);
        //                if (StringUtils.isNotBlank(titleHighLight)) {
        //                    postIdTitleHighLight.put(postId, titleHighLight);
        //                }
        //            }
        //        });
        //====================================
        Page<Post> page = new Page<>();
        page.setTotal(searchHits.getTotalHits());
        List<Post> resourceList = new ArrayList<>();
        // 查出结果后，从 db 获取最新动态数据（比如点赞数）
        if (searchHits.hasSearchHits()) {
            List<SearchHit<PostEsDTO>> searchHitList = searchHits.getSearchHits();

            // 搜索关键词高亮
            Map<Long, PostEsHighlightData> highlightDataMap = new HashMap<>();
            for (SearchHit<PostEsDTO> hit : searchHits.getSearchHits()) {
                PostEsHighlightData data = new PostEsHighlightData();
                data.setId(Long.valueOf(hit.getId()));
                List<String> titleHigh = hit.getHighlightFields().get("title");
                if (CollectionUtils.isNotEmpty(titleHigh)) {
                    String highlightTitle = String.valueOf(titleHigh);
                    data.setTitle(highlightTitle.substring(1, highlightTitle.length() - 1));
                    System.out.println(data.getTitle());
                }
                List<String> contentHigh = hit.getHighlightFields().get("content");
                if (CollectionUtils.isNotEmpty(contentHigh)) {
                    String highlightContent = String.valueOf(contentHigh);
                    data.setContent(highlightContent.substring(1, highlightContent.length() - 1));
                    System.out.println(data.getContent());
                }
                highlightDataMap.put(data.getId(), data);
            }
            //获取id列表
            List<Long> postIdList = searchHitList.stream().map(searchHit -> searchHit.getContent().getId())
                    .collect(Collectors.toList());
            // 根据id查找数据集
            List<Post> postList = baseMapper.selectBatchIds(postIdList);
            if (postList != null) {
                Map<Long, List<Post>> idPostMap = postList.stream().collect(Collectors.groupingBy(Post::getId));
                postIdList.forEach(postId -> {
                    if (idPostMap.containsKey(postId)) {
                        // 搜索关键词高亮替换
                        Post post = idPostMap.get(postId).get(0);
                        String hlTitle = highlightDataMap.get(postId).getTitle();
                        String hlContent = highlightDataMap.get(postId).getContent();
                        if (StringUtils.isNotBlank(hlTitle)) {
                            post.setTitle(hlTitle);
                        }
                        if (StringUtils.isNotBlank(hlContent)) {
                            post.setContent(hlContent);
                        }
                        resourceList.add(post);
                        //resourceList.add(idPostMap.get(postId).get(0));
                        //==========================================
                        //                        Post post = idPostMap.get(postId).get(0);
                        //                        // 给结果加上highlight，内容和标题都加上
                        //                        String contentHighLight = postIdContentHighLight.get(postId);
                        //                        if (StringUtils.isNotBlank(contentHighLight)) {
                        //                            post.setContent(contentHighLight);
                        //                        }
                        //                        String titleHighLight = postIdTitleHighLight.get(postId);
                        //                        if (StringUtils.isNotBlank(titleHighLight)) {
                        //                            post.setTitle(titleHighLight);
                        //                        }
                        //                        resourceList.add(post);
                        //============================================
                    } else {
                        // 从 es 清空 db 已物理删除的数据
                        String delete = elasticsearchRestTemplate.delete(String.valueOf(postId), PostEsDTO.class);
                        log.info("delete post {}", delete);
                    }
                });
            }
        }
        page.setRecords(resourceList);
        return page;
    }

    @Override
    public PostVO getPostVO(Post post, HttpServletRequest request) {
        PostVO postVO = PostVO.objToVo(post);
        long postId = post.getId();
        // 1. 关联查询用户信息
        Long userId = post.getUserId();
        User user = null;
        if (userId != null && userId > 0) {
            user = userService.getById(userId);
        }
        UserVO userVO = userService.getUserVO(user);
        postVO.setUser(userVO);
        // 2. 已登录，获取用户点赞、收藏状态
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postId);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            PostThumb postThumb = postThumbMapper.selectOne(postThumbQueryWrapper);
            postVO.setHasThumb(postThumb != null);
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postId);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            PostFavour postFavour = postFavourMapper.selectOne(postFavourQueryWrapper);
            postVO.setHasFavour(postFavour != null);
        }
        return postVO;
    }

    @Override
    public Page<PostVO> getPostVOPage(Page<Post> postPage, HttpServletRequest request) {
        List<Post> postList = postPage.getRecords();
        Page<PostVO> postVOPage = new Page<>(postPage.getCurrent(), postPage.getSize(), postPage.getTotal());
        if (CollectionUtils.isEmpty(postList)) {
            return postVOPage;
        }
        // 1. 关联查询用户信息
        Set<Long> userIdSet = postList.stream().map(Post::getUserId).collect(Collectors.toSet());
        Map<Long, List<User>> userIdUserListMap = userService.listByIds(userIdSet).stream()
                .collect(Collectors.groupingBy(User::getId));
        // 2. 已登录，获取用户点赞、收藏状态
        Map<Long, Boolean> postIdHasThumbMap = new HashMap<>();
        Map<Long, Boolean> postIdHasFavourMap = new HashMap<>();
        User loginUser = userService.getLoginUserPermitNull(request);
        if (loginUser != null) {
            Set<Long> postIdSet = postList.stream().map(Post::getId).collect(Collectors.toSet());
            loginUser = userService.getLoginUser(request);
            // 获取点赞
            QueryWrapper<PostThumb> postThumbQueryWrapper = new QueryWrapper<>();
            postThumbQueryWrapper.in("postId", postIdSet);
            postThumbQueryWrapper.eq("userId", loginUser.getId());
            List<PostThumb> postPostThumbList = postThumbMapper.selectList(postThumbQueryWrapper);
            postPostThumbList.forEach(postPostThumb -> postIdHasThumbMap.put(postPostThumb.getPostId(), true));
            // 获取收藏
            QueryWrapper<PostFavour> postFavourQueryWrapper = new QueryWrapper<>();
            postFavourQueryWrapper.in("postId", postIdSet);
            postFavourQueryWrapper.eq("userId", loginUser.getId());
            List<PostFavour> postFavourList = postFavourMapper.selectList(postFavourQueryWrapper);
            postFavourList.forEach(postFavour -> postIdHasFavourMap.put(postFavour.getPostId(), true));
        }
        // 填充信息
        List<PostVO> postVOList = postList.stream().map(post -> {
            PostVO postVO = PostVO.objToVo(post);
            Long userId = post.getUserId();
            User user = null;
            if (userIdUserListMap.containsKey(userId)) {
                user = userIdUserListMap.get(userId).get(0);
            }
            postVO.setUser(userService.getUserVO(user));
            postVO.setHasThumb(postIdHasThumbMap.getOrDefault(post.getId(), false));
            postVO.setHasFavour(postIdHasFavourMap.getOrDefault(post.getId(), false));
            return postVO;
        }).collect(Collectors.toList());
        postVOPage.setRecords(postVOList);
        return postVOPage;
    }

    @Override
    public Page<PostVO> listPostVOByPage(PostQueryRequest postQueryRequest, HttpServletRequest request) {
        long current = postQueryRequest.getPageNum();
        long size = postQueryRequest.getPageSize();
        Page<Post> postPage = this.page(new Page<>(current, size),
                this.getQueryWrapper(postQueryRequest));
        return this.getPostVOPage(postPage, request);
    }

}




