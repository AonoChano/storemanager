package com.wjy.storemanager.config;

import com.wjy.storemanager.interceptor.LoginInterceptor;
import com.wjy.storemanager.vo.LoginVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private LoginInterceptor loginInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginInterceptor)
                .addPathPatterns(
                        "/product/**", "/category/**", "/supplier/**",
                        "/purchaseOrder/**", "/saleOrder/**", "/saleOrderDetail/**",
                        "/stockRecord/**", "/report/**", "/operationLog/**",
                        "/chat/**")
                .excludePathPatterns("/user/login", "/user/logout");
    }
}
