package com.pani.searchsoso.esdao;

import com.pani.searchsoso.model.dto.post.PostEsDTO;

import java.util.List;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * 帖子 ES 操作
 *ElasticsearchRepository<PostEsDTO, Long>，默认提供了简单的增删改查，多用于可预期的、
 * 相对没那么复杂的查询、自定义查询，返回结果相对简单直接。
 * @author Pani
 */
public interface PostEsDao extends ElasticsearchRepository<PostEsDTO, Long> {

    List<PostEsDTO> findByUserId(Long userId);

    List<PostEsDTO> findByTitle(String title);
}