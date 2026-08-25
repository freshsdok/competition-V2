package com.teaching.system.mapper;

import com.teaching.system.domain.WorkOrder;
import com.teaching.system.domain.WorkOrderTransfer;

import java.util.List;

/**
 * 工单信息Mapper接口
 *
 * @author teaching
 * @date 2025-10-30
 */
public interface WorkOrderMapper {
    /**
     * 查询工单信息
     *
     * @param orderId 工单信息主键
     * @return 工单信息
     */
    public WorkOrder selectWorkOrderByOrderId(Long orderId);

    /**
     * 查询工单信息列表
     *
     * @param workOrder 工单信息
     * @return 工单信息集合
     */
    public List<WorkOrder> selectWorkOrderList(WorkOrder workOrder);

    /**
     * 查询工单信息列表 转给我的
     * @param workOrder
     * @return
     */
    public List<WorkOrder> selectWorkOrderListTransferToMe(WorkOrder workOrder);

    /**
     * 新增工单信息
     *
     * @param workOrder 工单信息
     * @return 结果
     */
    public int insertWorkOrder(WorkOrder workOrder);

    /**
     * 修改工单信息
     *
     * @param workOrder 工单信息
     * @return 结果
     */
    public int updateWorkOrder(WorkOrder workOrder);

    /**
     * 删除工单信息
     *
     * @param orderId 工单信息主键
     * @return 结果
     */
    public int deleteWorkOrderByOrderId(Long orderId);

    /**
     * 批量删除工单信息
     *
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWorkOrderByOrderIds(Long[] orderIds);

    /**
     * 批量删除工单转单记录
     *
     * @param orderIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteWorkOrderTransferByOrderIds(Long[] orderIds);

    /**
     * 批量新增工单转单记录
     *
     * @param workOrderTransferList 工单转单记录列表
     * @return 结果
     */
    public int batchWorkOrderTransfer(List<WorkOrderTransfer> workOrderTransferList);


    /**
     * 通过工单信息主键删除工单转单记录信息
     *
     * @param orderId 工单信息ID
     * @return 结果
     */
    public int deleteWorkOrderTransferByOrderId(Long orderId);



    /**
     * 新增工单转单记录
     *
     * @param workOrderTransfer 工单转单记录
     * @return 结果
     */
    public int insertWorkOrderTransfer(WorkOrderTransfer workOrderTransfer);
}
