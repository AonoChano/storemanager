package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.Supplier;

import java.util.List;

public interface SupplierService {
    //删除
    int delete(long id);
    //增加
    int insert(Supplier supplier);
    //精准查询
    Supplier selectByPrimaryKey(long id);
    //返回所有
    List<Supplier> selectAll();
    //修改
    int updateByPrimaryKey(Supplier supplier);
}
