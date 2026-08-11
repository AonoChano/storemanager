package com.wjy.storemanager.controller;

import com.wjy.storemanager.annotation.Log;
import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.SaleOrder;
import com.wjy.storemanager.service.SaleOrderService;
import com.wjy.storemanager.vo.SaleReportVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/saleOrder")
public class SaleOrderController {
    @Autowired
    private SaleOrderService saleOrderService;

    //销售出库
    @PostMapping("/sale")
    @Log("销售出库")
    public Result<Void> saleOrder(@RequestBody SaleOrder saleOrder){
        saleOrderService.saleOrder(saleOrder);
        return Result.success();

    }



    //视图:根据天数总金额
    @GetMapping("/voByDay")
    public Result<List<SaleReportVo>> saleReportVoByDay(){
        return Result.success(saleOrderService.saleReportVoByDay());
    }

}
