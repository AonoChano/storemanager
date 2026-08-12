package com.wjy.storemanager.common;

import lombok.Data;
import org.springframework.objenesis.instantiator.perc.PercInstantiator;

/**
 * 统一返回结果
 * code=1成功
 * 失败是0
 * @param <T>
 */
@Data
public class Result<T> {
    private Integer code;
    private String msg;
    private T data;

    public static <T> Result<T> success(T data){
        Result<T> result= new  Result<>();
        result.setCode(1);
        result.setMsg("成功");
        result.setData(data);
        return result;
    }

    /**sale_order_detail
     * Result<Void>使用return Result.success()
     * @return
     * @param <T>
     */
    public static <T> Result<T> success(){
        return success(null);
    }

    public static <T> Result<T> error(String msg){
        Result<T> result=new Result<>();
        result.setCode(0);
        result.setMsg(msg);
        return result;
    }
}
