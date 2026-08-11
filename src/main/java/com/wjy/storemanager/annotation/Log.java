package com.wjy.storemanager.annotation;
import java.lang.annotation.*;
@Target(ElementType.METHOD)//限制只能加方法注解
@Retention(RetentionPolicy.RUNTIME)
public @interface Log {
    String value() default "";//操作描述
}
