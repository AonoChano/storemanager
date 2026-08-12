package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.User;
import com.wjy.storemanager.service.UserService;
import com.wjy.storemanager.vo.LoginVo;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.PrimitiveIterator;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@RestController
@RequestMapping("/user")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    //登录方法,用uuid当作token验证身份
    @PostMapping("/login")
    public Result<LoginVo> login(@RequestBody User user) {
        User u = userService.login(user.getUsername(), user.getPassword());
        String token = UUID.randomUUID().toString().replace("-", "");
        stringRedisTemplate.opsForValue().set("token:" + token, String.valueOf(u.getId()), 30, TimeUnit.MINUTES);
        LoginVo vo = new LoginVo();
        vo.setToken(token);
        vo.setUser(u);
        return Result.success(vo);
    }


    //退出登录,删除redis中的token
    @PostMapping("/logout")
    public Result<LoginVo> loginOut(HttpServletRequest request) {
        String auth = request.getHeader("authorization");
        if (auth != null && auth.startsWith("Bearer ")) {
            stringRedisTemplate.delete("token:" + auth.substring(7));
        }
        return Result.success();
    }


}
