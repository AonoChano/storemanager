package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.mapper.SaleOrderDetailMapper;
import com.wjy.storemanager.service.SaleOrderDetailService;
import com.wjy.storemanager.vo.ProfitVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SaleOrderDetailServiceImpl implements SaleOrderDetailService {
    @Autowired
    private SaleOrderDetailMapper saleOrderDetailMapper;
    @Override
    public ProfitVo profitReportVo() {
        return saleOrderDetailMapper.profitReportVo();
    }
}
