package com.pani.searchsoso.datasource;


import com.pani.searchsoso.model.enums.SearchTypeEnum;
import org.apache.poi.ss.formula.functions.T;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;

/**
 * @author Pani
 * {@code @date} Created in 2023/11/11 17:17
 * {@code @description}
 */
@Component
public class DataSourceRegistry {
    @Resource
    private PictureDataSource pictureDataSource;
    @Resource
    private UserDataSource userDataSource;
    @Resource
    private PostDataSource postDataSource;
    @Resource
    private VideoDataSource videoDataSource;

    private Map<String, DataSource<T>> typeDataSourcesMap;

    @PostConstruct
    public void doInit() {
        //原来还能这样写
        typeDataSourcesMap = new HashMap() {{
            put(SearchTypeEnum.POST.getValue(), postDataSource);
            put(SearchTypeEnum.USER.getValue(), userDataSource);
            put(SearchTypeEnum.PICTURE.getValue(), pictureDataSource);
            put(SearchTypeEnum.VIDEO.getValue(), videoDataSource);
        }};
    }

    public DataSource<T> getDataSourceByType(String type){
        if (typeDataSourcesMap == null) {
            return null;
        }
        return typeDataSourcesMap.get(type);
    }
}

