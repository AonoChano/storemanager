package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.entity.Product;
import com.wjy.storemanager.mapper.ProductMapper;
import com.wjy.storemanager.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
import java.util.PrimitiveIterator;

@Service
public class ProductServiceImpl implements ProductService {
    @Autowired
    private ProductMapper productMapper;
    @Override
    public int insert(Product product) {
        product.setCreateTime(new Date());
        if(product.getStatus()==null)product.setStatus((byte) 1);//商品在售状态
        if(product.getWarningThreshold()==null) product.setWarningThreshold(0);//数量预警阈值
        if(product.getStock()==null) product.setStock(0);//库存

        return productMapper.insert(product);
    }

    @Override
    public int delete(long id) {
        return productMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int update(Product product) {
        product.setUpdateTime(new Date());
        return productMapper.updateByPrimaryKey(product);
    }

    @Override
    public Product select(long id) {
        return productMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Product> selectAll(String name,Long categoryId,String barcode) {
        return productMapper.selectAll( name, categoryId, barcode);
    }


    @Override
    public List<Product> selectWarning() {
        return productMapper.selectWarning();
    }
}
