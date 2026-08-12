package com.wjy.storemanager.vo;

import lombok.Data;
import java.math.BigDecimal;

// 某一天: 采购额 vs 销售额(同轴对比)
@Data
public class PurchaseVsSaleVo {
    private String day;
    private BigDecimal purchaseAmount;
    private BigDecimal saleAmount;
}
