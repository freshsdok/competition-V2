package com.teaching.system.service;

import com.teaching.system.domain.WorkOrder;
import com.teaching.system.domain.WorkOrderTransfer;

import java.util.List;

/**
 * 工单信息Service接口
 *
 * @author teaching
 * @date 2025-10-30
 */
public interface IWorkOrderService {
    /**
     * 查询工单信息
     *
     * @param orderId 工单信息主键
     * @return 工单信息
     */
    public WorkOrder selectWorkOrderByOrderId(Long orderId);

    /**
     * 转单操作
     * @param workOrderTransfer
     * @return
     */
    public int transferWorkOrder(WorkOrderTransfer workOrderTransfer);

    /**
     * 处理操作
     * @param workOrder
     * @return
     */
    public int doDispose(WorkOrder workOrder);

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
     * 批量删除工单信息
     *
     * @param orderIds 需要删除的工单信息主键集合
     * @return 结果
     */
    public int deleteWorkOrderByOrderIds(Long[] orderIds);

    /**
     * 删除工单信息信息
     *
     * @param orderId 工单信息主键
     * @return 结果
     */
    public int deleteWorkOrderByOrderId(Long orderId);
}
