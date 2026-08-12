package com.wjy.storemanager.vo;

import lombok.Data;

// 按分类汇总库存
@Data
public class CategoryStockVo {
    private String name;   // 分类名
    private Long stock;    // 该分类商品库存总和
}
