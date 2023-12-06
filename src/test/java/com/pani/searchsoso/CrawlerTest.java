package com.pani.searchsoso;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;

import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.pani.searchsoso.model.entity.Picture;
import com.pani.searchsoso.model.entity.Post;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.List;
import java.util.Map;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/9 19:15
 * {@code @description}
 */
@SpringBootTest
public class CrawlerTest {
    public static void main(String[] args) {
        // 1. 获取数据
        String json = "{\"current\": 2, \"pageSize\": 8, \"sortField\": \"createTime\", " +
                "\"sortOrder\": \"descend\", \"category\": \"文章\",\"reviewStatus\": 1}";
        String url = "https://www.code-nav.cn/api/post/search/page/vo";
        String result = HttpRequest
                .post(url)
                .body(json)
                .execute()
                .body();
        Map<String,Object> map = JSONUtil.toBean(result, Map.class);
        JSONObject data = (JSONObject) map.get("data");
        JSONArray records = (JSONArray) data.get("records");

        ArrayList<Post> postList = new ArrayList<>();
        for (Object record : records) {
            JSONObject tempRec = (JSONObject)record;
            Post post = new Post();
            post.setId(Long.parseLong(tempRec.getStr("id")));
            post.setTitle(tempRec.getStr("title"));
            post.setContent(tempRec.getStr("content"));
            JSONArray tags = (JSONArray)tempRec.get("tags");
            List<String> tagsList = tags.toList(String.class);
            post.setTags(JSONUtil.toJsonStr(tagsList));
            post.setUserId(1L);
            postList.add(post);
        }
        System.out.println(postList);
    }

    @Test
    public void getpics() throws IOException {
        int current = 1;
        String url = "https://www.bing.com/images/search?q=巧克力&first=" + current;
        Document doc = Jsoup.connect(url).get();
        Elements elements = doc.select(".iuscp.isv");
        ArrayList<Picture> pictures = new ArrayList<>();

        for (Element element : elements) {
            // 取图片地址 (murl)
            String m = element.select(".iusc").get(0).attr("m");
            Map<String, Object> map = JSONUtil.toBean(m, Map.class);
            String murl = (String) map.get("murl");

            // 取标题
            String title = element.select(".inflnk").get(0).attr("aria-label");

            Picture picture = new Picture();
            picture.setTitle(title);
            picture.setUrl(murl);
            pictures.add(picture);
        }
        System.out.println(pictures);
    }
}
