package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.entity.*;
import com.wjy.storemanager.mapper.ProductMapper;
import com.wjy.storemanager.mapper.SaleOrderDetailMapper;
import com.wjy.storemanager.mapper.SaleOrderMapper;
import com.wjy.storemanager.mapper.StockRecordMapper;
import com.wjy.storemanager.service.SaleOrderService;
import com.wjy.storemanager.vo.SaleReportVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Service
public class SaleOrderServiceImpl implements SaleOrderService {
    @Autowired
    private SaleOrderMapper saleOrderMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private SaleOrderDetailMapper detailMapper;
    @Autowired
    private StockRecordMapper stockRecordMapper;

    /**
     * 出库销售方法,原子化操作
     * @param order
     */
    @Override
    @Transactional
    public void saleOrder(SaleOrder order) {
        //遍历order,计算金额
        BigDecimal total=BigDecimal.ZERO;
        for(SaleOrderDetail detail:order.getDetails()){
            Integer trueQuantity= productMapper.selectByPrimaryKey((detail.getProductId())).getStock();
            if(trueQuantity==null||trueQuantity<detail.getQuantity()){
                throw new RuntimeException("库存不足,当前库存为:"+productMapper.selectByPrimaryKey((detail.getProductId())).getName()+"仅剩:"+trueQuantity);
            }
            detail.setAmount(detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));//每件商品的总价
            total=total.add(detail.getAmount());

        }

        //初始化订单
        order.setTotalAmount(total);
        order.setOrderNo("S0"+System.currentTimeMillis());
        order.setStatus((byte)0);//待出库0,已出库是1
        order.setRemark("销售出库");
        order.setCreateTime(new Date());

        saleOrderMapper.insert(order);
        //将出库的对应product扣库存
        for(SaleOrderDetail detail: order.getDetails()){
            detail.setOrderId(order.getId());
            detailMapper.insert(detail);
            productMapper.updateStock(-detail.getQuantity(),detail.getProductId());
            //记录流水
            StockRecord record=new StockRecord();
            record.setProductId(detail.getProductId());
            record.setType((byte)1);
            record.setQuantity(detail.getQuantity());
            record.setBizType("销售出库");
            record.setOperatorId(order.getOperatorId());
            record.setOrderNo(order.getOrderNo());
            Integer newStock=productMapper.selectByPrimaryKey(detail.getProductId()).getStock();
            record.setAfterStock(newStock);
            stockRecordMapper.insert(record);
        }

    }


    @Override
    public List<SaleReportVo> saleReportVoByDay() {
       return   saleOrderMapper.saleReportVoByDay();
    }
}
