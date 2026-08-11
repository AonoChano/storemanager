package com.wjy.storemanager.controller;

import com.wjy.storemanager.common.Result;
import com.wjy.storemanager.service.SaleOrderDetailService;
import com.wjy.storemanager.vo.ProfitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/saleOrderDetail")
public class SaleOrderDetailController {
    @Autowired
    private SaleOrderDetailService saleOrderDetailService;
    //毛利方法
    @GetMapping("/profit")
    public Result<ProfitVo> profitReportVo(){
        return Result.success(saleOrderDetailService.profitReportVo());
    }
}
