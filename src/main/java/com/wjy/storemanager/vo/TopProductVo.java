package com.wjy.storemanager.vo;

import lombok.Data;
import java.math.BigDecimal;

// 商品销售排行(TOP N)
@Data
public class TopProductVo {
    private String name;       // 商品名
    private BigDecimal amount; // 累计销售额
}
