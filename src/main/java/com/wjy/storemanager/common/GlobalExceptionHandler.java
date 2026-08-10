package com.wjy.storemanager.common;

import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice//这个注解让controller异常全部走这里
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public Result<Void>handleException(Exception e){

        e.printStackTrace();
        return Result.error("系统异常:"+e.getMessage());
    }
}
