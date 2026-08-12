package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.mapper.ReportMapper;
import com.wjy.storemanager.service.ReportService;
import com.wjy.storemanager.vo.CategoryStockVo;
import com.wjy.storemanager.vo.PurchaseVsSaleVo;
import com.wjy.storemanager.vo.TopProductVo;
import com.wjy.storemanager.vo.TrendVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class ReportServiceImpl implements ReportService {

    @Autowired
    private ReportMapper reportMapper;

    @Override
    public List<TrendVo> saleTrend() {
        return reportMapper.saleTrend();
    }

    @Override
    public List<PurchaseVsSaleVo> purchaseVsSale() {
        // 采购按日 + 销售按日 → 按天合并成一行
        Map<String, PurchaseVsSaleVo> map = new LinkedHashMap<>();
        for (TrendVo p : reportMapper.purchaseByDay()) {
            PurchaseVsSaleVo vo = map.computeIfAbsent(p.getDay(), k -> new PurchaseVsSaleVo());
            vo.setDay(p.getDay());
            vo.setPurchaseAmount(p.getAmount());
        }
        for (TrendVo s : reportMapper.saleByDay()) {
            PurchaseVsSaleVo vo = map.computeIfAbsent(s.getDay(), k -> new PurchaseVsSaleVo());
            vo.setDay(s.getDay());
            vo.setSaleAmount(s.getAmount());
        }
        return new ArrayList<>(map.values());
    }

    @Override
    public List<TopProductVo> topProducts() {
        return reportMapper.topProducts();
    }

    @Override
    public List<CategoryStockVo> categoryStock() {
        return reportMapper.categoryStock();
    }
}
