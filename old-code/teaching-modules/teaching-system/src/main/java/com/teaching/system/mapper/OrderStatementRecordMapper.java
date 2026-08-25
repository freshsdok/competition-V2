package com.teaching.system.mapper;

import java.util.List;
import com.teaching.system.domain.OrderStatementRecord;
import org.apache.ibatis.annotations.Mapper;

/**
 * 对账单对账记录Mapper接口
 * 
 * @author teaching
 * @date 2025-10-27
 */
@Mapper
public interface OrderStatementRecordMapper 
{
    /**
     * 查询对账单对账记录
     * 
     * @param id 对账单对账记录主键
     * @return 对账单对账记录
     */
    public OrderStatementRecord selectOrderStatementRecordById(Long id);

    /**
     * 查询对账单对账记录列表
     * 
     * @param orderStatementRecord 对账单对账记录
     * @return 对账单对账记录集合
     */
    public List<OrderStatementRecord> selectOrderStatementRecordList(OrderStatementRecord orderStatementRecord);

    /**
     * 新增对账单对账记录
     * 
     * @param orderStatementRecord 对账单对账记录
     * @return 结果
     */
    public int insertOrderStatementRecord(OrderStatementRecord orderStatementRecord);

    /**
     * 修改对账单对账记录
     * 
     * @param orderStatementRecord 对账单对账记录
     * @return 结果
     */
    public int updateOrderStatementRecord(OrderStatementRecord orderStatementRecord);

    /**
     * 删除对账单对账记录
     * 
     * @param id 对账单对账记录主键
     * @return 结果
     */
    public int deleteOrderStatementRecordById(Long id);

    /**
     * 批量删除对账单对账记录
     * 
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOrderStatementRecordByIds(Long[] ids);

    Integer selectOrderStatementRecordByBillDate(String billDate);
}
