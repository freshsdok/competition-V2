package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.OrderGoodsRelation;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

/**
 * 订单商品关联Mapper接口
 * 
 * @author teaching
 * @date 2025-12-08
 */
@Mapper
public interface OrderGoodsRelationMapper 
{
    /**
     * 查询订单商品关联
     * 
     * @param id 订单商品关联主键
     * @return 订单商品关联
     */
    public OrderGoodsRelation selectOrderGoodsRelationById(Long id);

    /**
     * 查询订单商品关联列表
     * 
     * @param orderGoodsRelation 订单商品关联
     * @return 订单商品关联集合
     */
    public List<OrderGoodsRelation> selectOrderGoodsRelationList(OrderGoodsRelation orderGoodsRelation);

    /**
     * 新增订单商品关联
     * 
     * @param orderGoodsRelation 订单商品关联
     * @return 结果
     */
    public int insertOrderGoodsRelation(OrderGoodsRelation orderGoodsRelation);

    /**
     * 批量插入订单商品关联信息
     */
    public int insertOrderGoodsRelationBatch(@Param("list") List<OrderGoodsRelation> orderGoodsRelations);

    /**
     * 修改订单商品关联
     * 
     * @param orderGoodsRelation 订单商品关联
     * @return 结果
     */
    public int updateOrderGoodsRelation(OrderGoodsRelation orderGoodsRelation);

    /**
     * 删除订单商品关联
     * 
     * @param id 订单商品关联主键
     * @return 结果
     */
    public int deleteOrderGoodsRelationById(Long id);

    /**
     * 批量删除订单商品关联
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderGoodsRelationByIds(Long[] ids);

    List<OrderGoodsRelation> selectByOrderId(Long orderId);

    List<OrderGoodsRelation> selectByOrderIdAndCommodityId(@Param("orderId") Long orderId,@Param("commodityId") String commodityId);

    int deleteByOrderId(@Param("orderId") String orderId);

    int updateChangeType(String orderId,String teamCode, String changeType);

    int updatePayStatus(String orderId, String teamCode, String payStatus);

    List<OrderGoodsRelation> selectByCommodityId(String teamCode);
}
