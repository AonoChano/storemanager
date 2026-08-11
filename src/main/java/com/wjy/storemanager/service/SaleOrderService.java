package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.SaleOrder;
import com.wjy.storemanager.vo.SaleReportVo;

import java.util.List;

public interface SaleOrderService {
    //销售出库方法

    void saleOrder(SaleOrder order);
    List<SaleReportVo> saleReportVoByDay();
}
