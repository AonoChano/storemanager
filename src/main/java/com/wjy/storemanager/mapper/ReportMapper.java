package com.wjy.storemanager.mapper;

import com.wjy.storemanager.vo.CategoryStockVo;
import com.wjy.storemanager.vo.TopProductVo;
import com.wjy.storemanager.vo.TrendVo;
import org.apache.ibatis.annotations.Select;

import java.util.List;

// 报表聚合查询(跨多张表, 单独放一个 Mapper)
public interface ReportMapper {

    // 销售按日趋势
    @Select("select date_format(create_time,'%Y-%m-%d') as day, sum(total_amount) as amount " +
            "from sale_order where status = 1 " +
            "group by date_format(create_time,'%Y-%m-%d') order by day")
    List<TrendVo> saleTrend();

    // 采购按日(给 采购vs销售 用)
    @Select("select date_format(create_time,'%Y-%m-%d') as day, sum(total_amount) as amount " +
            "from purchase_order where status = 1 " +
            "group by date_format(create_time,'%Y-%m-%d') order by day")
    List<TrendVo> purchaseByDay();

    // 销售按日(给 采购vs销售 用)
    @Select("select date_format(create_time,'%Y-%m-%d') as day, sum(total_amount) as amount " +
            "from sale_order where status = 1 " +
            "group by date_format(create_time,'%Y-%m-%d') order by day")
    List<TrendVo> saleByDay();

    // 商品销售 TOP5(按销售额排行)
    @Select("select p.name as name, sum(d.amount) as amount " +
            "from sale_order_detail d " +
            "join product p on d.product_id = p.id " +
            "group by d.product_id, p.name " +
            "order by amount desc limit 5")
    List<TopProductVo> topProducts();

    // 各分类库存汇总(没分类的商品 left join 后 name 为 null)
    @Select("select c.name as name, sum(p.stock) as stock " +
            "from product p " +
            "left join category c on p.category_id = c.id " +
            "group by p.category_id, c.name")
    List<CategoryStockVo> categoryStock();
}
