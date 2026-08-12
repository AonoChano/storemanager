package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.common.CacheKeys;
import com.wjy.storemanager.entity.Product;
import com.wjy.storemanager.mapper.ProductMapper;
import com.wjy.storemanager.service.ProductService;
import org.apache.catalina.startup.Tool;
import org.apache.ibatis.ognl.internal.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.core.JacksonException;

import java.util.Date;
import java.util.List;
import java.util.PrimitiveIterator;
import java.util.concurrent.TimeUnit;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @Autowired
    private tools.jackson.databind.ObjectMapper objectMapper;

    private static final String PRODUCT_LIST_KEY="cache:product:list";

    @Autowired
    private ProductMapper productMapper;
    @Override
    public int insert(Product product) {
        product.setCreateTime(new Date());
        if(product.getStatus()==null)product.setStatus((byte) 1);//商品在售状态
        if(product.getWarningThreshold()==null) product.setWarningThreshold(0);//数量预警阈值
        if(product.getStock()==null) product.setStock(0);//库存

        var rows= productMapper.insert(product);
        stringRedisTemplate.delete(CacheKeys.PRODUCT_LIST);
        return rows;
    }

    @Override
    public int delete(long id) {

        var rows= productMapper.deleteByPrimaryKey(id);
        stringRedisTemplate.delete(CacheKeys.PRODUCT_LIST);
        return rows;
    }

    @Override
    public int update(Product product) {
        product.setUpdateTime(new Date());
        var rows= productMapper.updateByPrimaryKey(product);   // ① 先改库
        stringRedisTemplate.delete(CacheKeys.PRODUCT_LIST);    // ② 后删缓存
        return rows;
    }

    @Override
    public Product select(long id) {
        return productMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Product> selectAll(String name,Long categoryId,String barcode) {
        String cached=stringRedisTemplate.opsForValue().get(PRODUCT_LIST_KEY);
        if(name==null&&categoryId==null&&barcode==null){

            if(cached!=null){
                try {
                    return objectMapper.readValue(cached,
                            new tools.jackson.core.type.TypeReference<List<Product>>() {
                            });
                } catch (Exception e) {}
            }
            List<Product> list =productMapper.selectAll(null,null,null);
            try {
                stringRedisTemplate.opsForValue().set(PRODUCT_LIST_KEY,
                        objectMapper.writeValueAsString(list),10, TimeUnit.MINUTES);
            } catch (Exception e) {}
            return list;
        }


        return productMapper.selectAll( name, categoryId, barcode);
    }


    @Override
    public List<Product> selectWarning() {
        return productMapper.selectWarning();
    }
}
