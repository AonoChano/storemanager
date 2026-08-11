package com.wjy.storemanager.vo;

import com.wjy.storemanager.entity.User;
import com.wjy.storemanager.mapper.UserMapper;
import lombok.Data;

@Data
public class LoginVo {
    private User user;
    private String token;
}
