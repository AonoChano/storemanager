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
public class ProfitVo {
    private BigDecimal revenue;//销售金额
    private BigDecimal cost;//成本金额
    private BigDecimal profit;//总毛利

}
