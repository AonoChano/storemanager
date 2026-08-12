package com.wjy.storemanager.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice//这个注解让controller异常全部走这里
public class GlobalExceptionHandler {

    // 数据库访问异常(连接失败/查询出错): 记日志, 给前端统一短文案, 不暴露内部细节
    @ExceptionHandler(DataAccessException.class)
    public Result<Void> handleDataAccess(DataAccessException e) {
        log.error("数据库访问异常", e);
        return Result.error("数据库服务异常，请稍后再试");
    }

    // 业务异常(如"用户名不存在!"/"密码错误"): 直接把业务消息返回给前端
    @ExceptionHandler(RuntimeException.class)
    public Result<Void> handleBusiness(RuntimeException e) {
        log.warn("业务异常: {}", e.getMessage());
        return Result.error(e.getMessage());
    }

    // 兜底: 其他意外异常只记日志, 不把原始堆栈返回给前端
    @ExceptionHandler(Exception.class)
    public Result<Void> handleException(Exception e) {
        log.error("系统异常", e);
        return Result.error("系统繁忙，请稍后再试");
    }
}
