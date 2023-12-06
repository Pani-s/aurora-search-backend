//package com.pani.searchsoso.service.impl;
//
//
//import cn.hutool.json.JSONUtil;
//import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
//import com.pani.searchsoso.common.ErrorCode;
//import com.pani.searchsoso.exception.BusinessException;
//import com.pani.searchsoso.model.entity.Picture;
//import com.pani.searchsoso.service.PictureService;
//import org.jsoup.Jsoup;
//import org.jsoup.nodes.Document;
//import org.jsoup.nodes.Element;
//import org.jsoup.select.Elements;
//import org.springframework.stereotype.Service;
//
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.Map;
//
///**
// * @author Pani
// * {@code @date} Created in 2023/11/9 20:11
// * {@code @description}
// */
////todo:删了。
////@Service
//public class PictureServiceImpl implements PictureService {
//    @Override
//    public Page<Picture> searchPicture(String searchText, long pageNum, long pageSize) {
//        //int current = 1;
//        long current = (pageNum - 1) * pageSize;
////        String url = "https://www.bing.com/images/search?q=" + searchText
////                + "&first=" + current;
//        String url = "https://www.bing.com/images/search?q=" + searchText
//                + "&first=" + 1;
//        Document doc = null;
//        try {
//            doc = Jsoup.connect(url).get();
//        } catch (IOException e) {
//            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
//                    "数据获取异常,获取图片的时候失败了");
//        }
//        Elements elements = doc.select(".iuscp.isv");
//        ArrayList<Picture> pictures = new ArrayList<>();
//
//        for (Element element : elements) {
//            // 取图片地址 (murl)
//            String m = element.select(".iusc").get(0).attr("m");
//            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
//            String murl = (String) map.get("murl");
//
//            // 取标题
//            String title = element.select(".inflnk").get(0).attr("aria-label");
//
//            Picture picture = new Picture();
//            picture.setTitle(title);
//            picture.setUrl(murl);
//            pictures.add(picture);
//            //只获取前pageSize个图片
//            if (pictures.size() >= pageSize) {
//                break;
//            }
//        }
//        Page<Picture> page = new Page<>(pageNum, pageSize);
//        page.setRecords(pictures);
//        return page;
//    }
//}
