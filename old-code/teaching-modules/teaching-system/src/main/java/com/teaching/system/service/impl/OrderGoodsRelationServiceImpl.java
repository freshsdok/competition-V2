package com.teaching.system.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.OrderGoodsRelationMapper;
import com.teaching.system.domain.OrderGoodsRelation;
import com.teaching.system.service.IOrderGoodsRelationService;

/**
 * 订单商品关联Service业务层处理
 * 
 * @author teaching
 * @date 2025-12-08
 */
@Service
public class OrderGoodsRelationServiceImpl implements IOrderGoodsRelationService 
{
    @Autowired
    private OrderGoodsRelationMapper orderGoodsRelationMapper;

    /**
     * 查询订单商品关联
     * 
     * @param id 订单商品关联主键
     * @return 订单商品关联
     */
    @Override
    public OrderGoodsRelation selectOrderGoodsRelationById(Long id)
    {
        return orderGoodsRelationMapper.selectOrderGoodsRelationById(id);
    }

    /**
     * 查询订单商品关联列表
     * 
     * @param orderGoodsRelation 订单商品关联
     * @return 订单商品关联
     */
    @Override
    public List<OrderGoodsRelation> selectOrderGoodsRelationList(OrderGoodsRelation orderGoodsRelation)
    {
        return orderGoodsRelationMapper.selectOrderGoodsRelationList(orderGoodsRelation);
    }

    /**
     * 新增订单商品关联
     * 
     * @param orderGoodsRelation 订单商品关联
     * @return 结果
     */
    @Override
    public int insertOrderGoodsRelation(OrderGoodsRelation orderGoodsRelation)
    {
        orderGoodsRelation.setCreateTime(DateUtils.getNowDate());
        return orderGoodsRelationMapper.insertOrderGoodsRelation(orderGoodsRelation);
    }

    /**
     * 修改订单商品关联
     * 
     * @param orderGoodsRelation 订单商品关联
     * @return 结果
     */
    @Override
    public int updateOrderGoodsRelation(OrderGoodsRelation orderGoodsRelation)
    {
        orderGoodsRelation.setUpdateTime(DateUtils.getNowDate());
        return orderGoodsRelationMapper.updateOrderGoodsRelation(orderGoodsRelation);
    }

    /**
     * 批量删除订单商品关联
     * 
     * @param ids 需要删除的订单商品关联主键
     * @return 结果
     */
    @Override
    public int deleteOrderGoodsRelationByIds(Long[] ids)
    {
        return orderGoodsRelationMapper.deleteOrderGoodsRelationByIds(ids);
    }

    /**
     * 删除订单商品关联信息
     * 
     * @param id 订单商品关联主键
     * @return 结果
     */
    @Override
    public int deleteOrderGoodsRelationById(Long id)
    {
        return orderGoodsRelationMapper.deleteOrderGoodsRelationById(id);
    }
}
