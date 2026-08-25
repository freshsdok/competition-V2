package com.teaching.system.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.OrderStatementRecordMapper;
import com.teaching.system.domain.OrderStatementRecord;
import com.teaching.system.service.IOrderStatementRecordService;

/**
 * 对账单对账记录Service业务层处理
 * 
 * @author teaching
 * @date 2025-10-27
 */
@Service
public class OrderStatementRecordServiceImpl implements IOrderStatementRecordService 
{
    @Autowired
    private OrderStatementRecordMapper orderStatementRecordMapper;

    /**
     * 查询对账单对账记录
     * 
     * @param id 对账单对账记录主键
     * @return 对账单对账记录
     */
    @Override
    public OrderStatementRecord selectOrderStatementRecordById(Long id)
    {
        return orderStatementRecordMapper.selectOrderStatementRecordById(id);
    }

    /**
     * 查询对账单对账记录列表
     * 
     * @param orderStatementRecord 对账单对账记录
     * @return 对账单对账记录
     */
    @Override
    public List<OrderStatementRecord> selectOrderStatementRecordList(OrderStatementRecord orderStatementRecord)
    {
        return orderStatementRecordMapper.selectOrderStatementRecordList(orderStatementRecord);
    }

    /**
     * 新增对账单对账记录
     * 
     * @param orderStatementRecord 对账单对账记录
     * @return 结果
     */
    @Override
    public int insertOrderStatementRecord(OrderStatementRecord orderStatementRecord)
    {
        orderStatementRecord.setCreateTime(DateUtils.getNowDate());
        return orderStatementRecordMapper.insertOrderStatementRecord(orderStatementRecord);
    }

    /**
     * 修改对账单对账记录
     * 
     * @param orderStatementRecord 对账单对账记录
     * @return 结果
     */
    @Override
    public int updateOrderStatementRecord(OrderStatementRecord orderStatementRecord)
    {
        orderStatementRecord.setUpdateTime(DateUtils.getNowDate());
        return orderStatementRecordMapper.updateOrderStatementRecord(orderStatementRecord);
    }

    /**
     * 批量删除对账单对账记录
     * 
     * @param ids 需要删除的对账单对账记录主键
     * @return 结果
     */
    @Override
    public int deleteOrderStatementRecordByIds(Long[] ids)
    {
        return orderStatementRecordMapper.deleteOrderStatementRecordByIds(ids);
    }

    /**
     * 删除对账单对账记录信息
     * 
     * @param id 对账单对账记录主键
     * @return 结果
     */
    @Override
    public int deleteOrderStatementRecordById(Long id)
    {
        return orderStatementRecordMapper.deleteOrderStatementRecordById(id);
    }
}
