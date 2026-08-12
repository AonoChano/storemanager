package com.wjy.storemanager.vo;

import lombok.Data;
import java.math.BigDecimal;

// 按日聚合: 某一天的金额(销售/采购通用)
@Data
public class TrendVo {
    private String day;          // 日期 2026-08-11
    private BigDecimal amount;   // 当日金额
}
