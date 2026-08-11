package com.wjy.storemanager.controller;

import com.wjy.storemanager.annotation.Log;
import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.entity.PurchaseOrder;
import com.wjy.storemanager.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/purchaseOrder")
public class PurchaseOrderController {
    @Autowired
    private PurchaseOrderService purchaseOrderService;
    @PostMapping("/create")
    @Log("采购入库")
    public Result<Void>createOrder(@RequestBody PurchaseOrder order){
        purchaseOrderService.createOrder(order);
        return Result.success();
    }


}
