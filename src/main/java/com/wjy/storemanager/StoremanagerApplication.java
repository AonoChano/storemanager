package com.wjy.storemanager;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
// 扫描 mapper 包, 让所有 Mapper 接口注册为 Spring 的 Bean(否则 Service 里注入会报错)
@MapperScan("com.wjy.storemanager.mapper")
public class StoremanagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(StoremanagerApplication.class, args);
    }

}
