package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.entity.Supplier;
import com.wjy.storemanager.mapper.SupplierMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.List;
@Service
public class SupplierServiceImpl implements com.wjy.storemanager.service.SupplierService {
    @Autowired
    private SupplierMapper supplierMapper;
    @Override
    public int delete(long id) {
        return supplierMapper.deleteByPrimaryKey(id);
    }

    @Override
    public int insert(Supplier supplier) {
        supplier.setCreateTime(new Date());
        return supplierMapper.insert(supplier);
    }

    @Override
    public Supplier selectByPrimaryKey(long id) {
        return supplierMapper.selectByPrimaryKey(id);
    }

    @Override
    public List<Supplier> selectAll() {
        return supplierMapper.selectAll();
    }

    @Override
    public int updateByPrimaryKey(Supplier supplier) {
        return supplierMapper.updateByPrimaryKey(supplier);
    }
}
