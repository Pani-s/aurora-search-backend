package com.pani.searchsoso.datasource;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.pani.searchsoso.model.dto.user.UserQueryRequest;
import com.pani.searchsoso.model.vo.UserVO;
import com.pani.searchsoso.service.UserService;
import org.springframework.stereotype.Service;
import javax.annotation.Resource;


/**
 * @author Pani
 * {@code @date} Created in 2023/11/11 17:08
 * {@code @description}用户数据源处理
 */
@Service
public class UserDataSource implements DataSource<UserVO>{
    @Resource
    private UserService userService;

    @Override
    public Page<UserVO> doSearch(String searchText, long pageNum, long pageSize) {
        UserQueryRequest userQueryRequest = new UserQueryRequest();
        userQueryRequest.setUserName(searchText);
        userQueryRequest.setPageNum(pageNum);
        userQueryRequest.setPageSize(pageSize);
        return userService.listUserVOByPage(userQueryRequest);
    }

}
