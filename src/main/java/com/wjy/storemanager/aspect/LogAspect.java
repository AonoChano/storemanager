package com.wjy.storemanager.aspect;

import com.wjy.storemanager.annotation.Log;
import com.wjy.storemanager.entity.OperationLog;
import com.wjy.storemanager.mapper.OperationLogMapper;
import jakarta.servlet.http.HttpServletRequest;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Date;

@Aspect
@Component
public class LogAspect {
    @Autowired
    private OperationLogMapper operationLogMapper;
    @Autowired
    private HttpServletRequest request;
    //环绕所有标@Log的方法
    @Around("@annotation(log)")
    public Object around(ProceedingJoinPoint pjp, Log log) throws Throwable {
        Object result=pjp.proceed();

        try {
            OperationLog op = new OperationLog();
            op.setUserId(getHeaderLong("X-User-id"));
            op.setUsername(getHeader("X-User-name"));
            op.setCreateTime(new Date());
            op.setModule(log.value());
            op.setAction(pjp.getSignature().getName());
            op.setIp(request.getRemoteAddr());
            operationLogMapper.insert(op);
        } catch (Exception e) {
            //空实现,不让日志失败影响业务
        }
        return result;
    }

    private String getHeader(String name){
        String v=request.getHeader(name);
        return (v==null||v.isEmpty())?null:v;
    }
    private  Long getHeaderLong(String name){
        String v=getHeader(name);
        return (v==null||v.isEmpty())?null:Long.valueOf(v);
    }






}
