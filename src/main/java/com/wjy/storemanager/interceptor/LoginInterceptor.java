package com.wjy.storemanager.interceptor;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.User;
import com.wjy.storemanager.mapper.UserMapper;
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
    @Autowired
    private UserMapper userMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if("OPTIONS".equalsIgnoreCase(request.getMethod())){

            return true;
        }
        String auth=request.getHeader("Authorization");
        String token =(auth!=null&& auth.startsWith("Bearer "))?auth.substring(7):null;
        String userId=(token==null)?null:stringRedisTemplate.opsForValue().get("token:"+token);
        if(userId==null){
            response.setContentType("application/json;charset=UTF-8");
            ObjectMapper om=new ObjectMapper();
            response.getWriter().write(om.writeValueAsString(Result.error("未登录")));
            return false;
        }
        User user= userMapper.selectByPrimaryKey(Long.valueOf(userId));
        if(user==null||!hasPermission(user.getRoleId(),request.getRequestURI())){
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write(
                    new ObjectMapper().writeValueAsString(Result.error("没有权限")));
                    return false;

        }
        request.setAttribute("userId",Long.valueOf(userId));
        request.setAttribute("roleId",user.getRoleId());
        return true;
    }


    private boolean hasPermission(Long roleId ,String uri){
        if(roleId==null) return false;
        if(uri.startsWith("/chat")) return true;//客服:所有登录用户可用
        if(roleId==1) return true;//管理员
        if(roleId==2){//采购员
            return uri.startsWith("/product")||uri.startsWith("/category")
                    ||uri.startsWith("/supplier")||uri.startsWith("/purchaseOrder")
                    ||uri.startsWith("/report")
                ||uri.startsWith("/saleOrderDetail");
        }



        if(roleId==3){//销售员
            return uri.startsWith("/product")||uri.startsWith("/saleOrder")
                    ||uri.startsWith("/saleOrderDetail")||uri.startsWith("/report");
        }
        if(roleId==4){//库管
            return uri.startsWith("/product")||uri.startsWith("/stockRecord")
                    ||uri.startsWith("/report");
        }
        return false;

    }



}
