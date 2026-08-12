package com.wjy.storemanager.service.impl;

import com.wjy.storemanager.common.CacheKeys;
import com.wjy.storemanager.entity.PurchaseOrder;
import com.wjy.storemanager.entity.PurchaseOrderDetail;
import com.wjy.storemanager.entity.StockRecord;
import com.wjy.storemanager.mapper.ProductMapper;
import com.wjy.storemanager.mapper.PurchaseOrderDetailMapper;
import com.wjy.storemanager.mapper.PurchaseOrderMapper;
import com.wjy.storemanager.mapper.StockRecordMapper;
import com.wjy.storemanager.service.PurchaseOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
public class PurchaseOrderServiceImpl implements PurchaseOrderService {
    @Autowired
    private PurchaseOrderMapper purchaseOrderMapper;
    @Autowired
    private PurchaseOrderDetailMapper detailMapper;
    @Autowired
    private ProductMapper productMapper;
    @Autowired
    private StockRecordMapper stockRecordMapper;
    @Autowired
    private StringRedisTemplate stringRedisTemplate;

    /**
     * 商品入库实现方法,原子化操作
     * @param Order
     */
    @Transactional
    @Override
    public void createOrder(PurchaseOrder Order) {
        //计算总金额使用bigdecimal
        BigDecimal total=BigDecimal.ZERO;//初始化"0"
        for(PurchaseOrderDetail detail: Order.getDetails()){//detail作为属性直接存在purchaseOrder里面别忘了
            if(detail.getPrice().signum()<=0){
                throw new RuntimeException("金额不能为负");
            }
            if(detail.getQuantity()<0){
                throw new RuntimeException("数量不能为负");
            }
            detail.setAmount( detail.getPrice().multiply(BigDecimal.valueOf(detail.getQuantity())));//计算出某一类商品:单价*总数
            total=total.add(detail.getAmount());
            }//到这里单条流水金额计算成功

        Order.setTotalAmount(total);
        Order.setOrderNo("P0"+System.currentTimeMillis());//订单编号用P0加时间戳,后期我想改成雪花算法
        Order.setStatus((byte)1);//状态已入库
        Order.setOperatorId(1L);//还没写登录先用1L
        Order.setCreateTime(new Date());
        //调用方法把初始化好的订单先插入purchaseOrder
        purchaseOrderMapper.insert(Order);

        //还要为product增加库存,话要把detail插进detail表里面
        for(PurchaseOrderDetail detail: Order.getDetails()){
        detail.setOrderId(Order.getId());//前面先插总表,再将总表的id传入这个明细
        detailMapper.insert(detail);//插入明细
        productMapper.updateStock(detail.getQuantity(),detail.getProductId());

        //记录流水
            StockRecord record=new StockRecord();
            record.setProductId(detail.getProductId());
            record.setType((byte)1);//1表示入库,0表示出库
            record.setQuantity(detail.getQuantity());
            record.setBizType("采购入库");
            record.setOrderNo(Order.getOrderNo());
            record.setOperatorId(Order.getOperatorId());
            record.setCreateTime(new Date());
            Integer newStock= productMapper.selectByPrimaryKey(detail.getProductId()).getStock();
            record.setAfterStock(newStock);
            stockRecordMapper.insert(record);

        }

        stringRedisTemplate.delete(CacheKeys.PRODUCT_LIST);
    }
















    }

