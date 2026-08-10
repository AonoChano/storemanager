package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.Product;

import java.util.List;

public interface ProductService {
    //增
    int insert(Product product);
    //删
    int delete(long id);
    //改
    int update(Product product);
    //查
    Product select(long id);
    //查所有
    List<Product> selectAll(String name,Long categoryId,String barcode);



}
