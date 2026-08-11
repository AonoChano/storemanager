package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.StockRecord;

import java.util.List;

public interface StockRecordService {
    //动态查询流水
    List<StockRecord> selectStockRecord(Long productId ,Byte type);


}
