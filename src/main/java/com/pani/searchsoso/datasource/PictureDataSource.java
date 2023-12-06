package com.pani.searchsoso.datasource;

import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.common.ErrorCode;
import com.pani.searchsoso.exception.BusinessException;
import com.pani.searchsoso.model.dto.user.UserQueryRequest;
import com.pani.searchsoso.model.entity.Picture;
import com.pani.searchsoso.model.vo.UserVO;
import com.pani.searchsoso.service.UserService;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/11 17:08
 * {@code @description}图片视频数据源处理
 */
@Service
public class PictureDataSource implements DataSource<Picture> {

    @Override
    public Page<Picture> doSearch(String searchText, long pageNum, long pageSize) {
        long current = (pageNum - 1) * pageSize;
        String url = "https://www.bing.com/images/search?q=" + searchText
                + "&first=" + current;
        //        String url = "https://www.bing.com/images/search?q=" + searchText
        //                + "&first=" + 1;
        Document doc = null;
        try {
            doc = Jsoup.connect(url).get();
        } catch (IOException e) {
            System.out.println("[WTM]：" + e);
            e.printStackTrace();
            throw new BusinessException(ErrorCode.SYSTEM_ERROR,
                    "数据获取异常,获取图片的时候失败了");
        }
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
            //只获取前pageSize个图片
            if (pictures.size() >= pageSize) {
                break;
            }
        }
        Page<Picture> page = new Page<>(pageNum, pageSize);
        page.setRecords(pictures);
        page.setTotal(999L);
        return page;
    }
}
