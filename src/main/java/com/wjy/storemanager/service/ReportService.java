package com.wjy.storemanager.service;

import com.wjy.storemanager.vo.CategoryStockVo;
import com.wjy.storemanager.vo.PurchaseVsSaleVo;
import com.wjy.storemanager.vo.TopProductVo;
import com.wjy.storemanager.vo.TrendVo;

import java.util.List;

public interface ReportService {
    List<TrendVo> saleTrend();

    List<PurchaseVsSaleVo> purchaseVsSale();

    List<TopProductVo> topProducts();

    List<CategoryStockVo> categoryStock();
}
