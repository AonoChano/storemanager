package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.StockRecord;
import com.wjy.storemanager.service.StockRecordService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/stockRecord")
public class StockRecordController {
    @Autowired
    private StockRecordService stockRecordService;

    //动态查询流水
    @GetMapping("/list")
    public Result<List<StockRecord>> selectStockRecord(@RequestParam(required = false)Long productId,
                                                      @RequestParam(required = false)Byte type){

        return Result.success(stockRecordService.selectStockRecord(productId,type));
    }
}
