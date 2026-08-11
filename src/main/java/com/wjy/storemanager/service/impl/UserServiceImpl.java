package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.entity.User;
import com.wjy.storemanager.mapper.UserMapper;
import com.wjy.storemanager.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {
    @Autowired
    private UserMapper userMapper;
    private final PasswordEncoder passwordEncoder=new BCryptPasswordEncoder();
    @Override
    public User login(String name, String password) {
        User user= userMapper.selectByUserName(name);
        if(user==null){
            throw new RuntimeException("用户名不存在!");
        }if (!passwordEncoder.matches(password,user.getPassword())){
            throw new RuntimeException("密码错误");
        }
        user.setPassword(null);
        return user;
    }
}
