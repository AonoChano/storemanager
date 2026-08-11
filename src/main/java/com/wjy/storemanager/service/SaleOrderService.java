package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.PurchaseOrder;
import com.wjy.storemanager.entity.SaleOrder;

public interface SaleOrderService {
    //销售出库方法

    void saleOrder(SaleOrder order);

}
