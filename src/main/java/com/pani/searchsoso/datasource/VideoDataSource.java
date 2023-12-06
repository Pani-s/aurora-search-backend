package com.pani.searchsoso.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.common.ErrorCode;
import com.pani.searchsoso.exception.BusinessException;
import com.pani.searchsoso.model.entity.Picture;
import com.pani.searchsoso.model.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/11 17:08
 * {@code @description}BILIBILI我来了~视频数据源处理
 */
@Service
public class VideoDataSource implements DataSource<Video> {

    @Override
    public Page<Video> doSearch(String searchText, long pageNum, long pageSize) {
        //long pageNo = (pageNum + 1) / 2;
        String url = "https://search.bilibili.com/video?keyword=" + searchText +
                "&page=" + pageNum;
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("[WTM]："+e.getMessage());
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "数据获取异常,获取B站视频的时候失败了");
        }
        Elements elements = doc.getElementsByClass("bili-video-card");
        ArrayList<Video> videos = new ArrayList<>();
        for (Element element : elements) {
            Video video = new Video();
            Elements herfEle = element.select(".bili-video-card__info--right");

            Element titleEle = herfEle.select(".bili-video-card__info--tit").get(0);
            String titleHtml = titleEle.html();
//            String titleHtml = titleEle.text();
            Element select1 = herfEle.select("a").get(0);
            String href = select1.attr("href");
            //==================================
            Elements imgEle = element.select(".bili-video-card__image--wrap");
            Element img = imgEle.select("img").get(0);
            String imgUrl = img.attr("src");
            video.setTitle(titleHtml);
            video.setVideoUrl(href);
            video.setPicUrl("https:"+imgUrl);
            videos.add(video);
        }
        Page<Video> page = new Page<>(pageNum, pageSize);
        page.setRecords(videos);
        page.setTotal(999);
        return page;
    }
}
