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
    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;
    @Override
    public int delete(long id) {
        // 外键引用检查: 已被采购单引用的供应商不允许物理删除
        Long refs = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM purchase_order WHERE supplier_id = ?", Long.class, id);
        if (refs != null && refs > 0) {
            throw new RuntimeException("该供应商已有采购单据，无法删除");
        }
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
