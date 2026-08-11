package com.wjy.storemanager.interceptor;

import com.wjy.storemanager.common.Result;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import tools.jackson.databind.ObjectMapper;

@Component
public class LoginInterceptor implements HandlerInterceptor {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("OPTIONS".equalsIgnoreCase(request.getMethod())){

            return true;
        }
        String auth=request.getHeader("Authorization");
        String token =(auth!=null&& auth.startsWith("Bearer "))?auth.substring(7):null;
        String userId=(token==null)?null:stringRedisTemplate.opsForValue().get("token:"+token);
        if(userId==null){
            response.setContentType("application/jsom;charset=UTF-8");
            ObjectMapper om=new ObjectMapper();
            response.getWriter().write(om.writeValueAsString(Result.error("未登录")));
            return false;
        }
        request.setAttribute("userId",Long.valueOf(userId));
        return true;
    }
}
