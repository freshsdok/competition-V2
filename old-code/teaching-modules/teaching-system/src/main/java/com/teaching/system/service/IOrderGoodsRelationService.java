package com.teaching.system.service;

import java.util.List;
import com.teaching.system.domain.OrderGoodsRelation;

/**
 * 订单商品关联Service接口
 * 
 * @author teaching
 * @date 2025-12-08
 */
public interface IOrderGoodsRelationService 
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
     * 修改订单商品关联
     * 
     * @param orderGoodsRelation 订单商品关联
     * @return 结果
     */
    public int updateOrderGoodsRelation(OrderGoodsRelation orderGoodsRelation);

    /**
     * 批量删除订单商品关联
     * 
     * @param ids 需要删除的订单商品关联主键集合
     * @return 结果
     */
    public int deleteOrderGoodsRelationByIds(Long[] ids);

    /**
     * 删除订单商品关联信息
     * 
     * @param id 订单商品关联主键
     * @return 结果
     */
    public int deleteOrderGoodsRelationById(Long id);
}
