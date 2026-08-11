package com.wjy.storemanager.service;

import com.wjy.storemanager.entity.PurchaseOrder;
import org.springframework.transaction.annotation.Transactional;

public interface PurchaseOrderService {

    //采购入库方法

    void createOrder(PurchaseOrder Order);





}
