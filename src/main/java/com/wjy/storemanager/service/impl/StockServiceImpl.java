package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.entity.StockRecord;
import com.wjy.storemanager.mapper.StockRecordMapper;
import com.wjy.storemanager.service.StockRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class StockServiceImpl implements StockRecordService {
    @Autowired
    private StockRecordMapper stockRecordMapper;
    @Override
    public List<StockRecord> selectStockRecord(Long productId, Byte type) {
        return stockRecordMapper.selectStockRecord(productId,type);
    }
}
