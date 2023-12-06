package com.pani.searchsoso.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.common.ErrorCode;
import com.pani.searchsoso.exception.BusinessException;
import com.pani.searchsoso.model.entity.Picture;
import com.pani.searchsoso.model.entity.Video;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/12 21:43
 * {@code @description}
 */
@SpringBootTest
public class VideoTest {
    static ArrayList<Video> videos = new ArrayList<>();
    static HashSet<String> set = new HashSet<>();

    public static void main(String[] args) {
        String url = null;
        url = "https://search.bilibili.com/video?keyword=冬天";
        crawlTest(url);
        url = "https://search.bilibili.com/video?keyword=冬天&page=2";
        crawlTest(url);
        System.out.println(videos.size());
        System.out.println(set.size());
    }
    @Test
    static void crawlTest(String url){
//        String url = null;
//        url = "https://search.bilibili.com/video?keyword=冬天";

        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "数据获取异常,获取B站视频的时候失败了");
        }
        //bili-video-card
        //bili-video-card__info __scale-disable
        //bili-video-card__info--right
        Elements elements = doc.getElementsByClass("bili-video-card");

        //        Elements cardElements = doc.getElementsByClass("bili-video-card__info--right");
        //        Elements titleClass = doc.getElementsByClass("bili-video-card__info--tit");


        for (Element element : elements) {
            Video video = new Video();
            Elements herfEle = element.select(".bili-video-card__info--right");

            Element titleEle = herfEle.select(".bili-video-card__info--tit").get(0);
            //String title = titleEle.text();
            String titleHtml = titleEle.html();
            Element select1 = herfEle.select("a").get(0);
            String href = select1.attr("href");
            //            System.out.println("href = "+href);
            //            System.out.println("title = "+titleHtml);
            //==================================
            Elements imgEle = element.select(".bili-video-card__image--wrap");
            Element img = imgEle.select("img").get(0);
            String imgUrl = img.attr("src");
            //            System.out.println("imgUrl = "+imgUrl);
            //            System.out.println("-=--------------------------------------");
            video.setTitle(titleHtml);
            video.setVideoUrl(href);
            video.setPicUrl(imgUrl);
            set.add(href);
            videos.add(video);
        }

    }
}

