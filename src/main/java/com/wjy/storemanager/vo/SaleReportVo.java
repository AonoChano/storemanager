package com.wjy.storemanager.vo;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class SaleReportVo {
    private String day;
    private Long orderCount;
    private BigDecimal totalAmount;

}
