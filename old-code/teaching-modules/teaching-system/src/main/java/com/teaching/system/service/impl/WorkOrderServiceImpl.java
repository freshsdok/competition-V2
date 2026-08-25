package com.teaching.system.service.impl;

import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysUser;
import com.teaching.system.domain.WorkOrder;
import com.teaching.system.domain.WorkOrderTransfer;
import com.teaching.system.mapper.WorkOrderMapper;
import com.teaching.system.service.IWorkOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * 工单信息Service业务层处理
 *
 * @author teaching
 * @date 2025-10-30
 */
@Service
public class WorkOrderServiceImpl implements IWorkOrderService {
    @Autowired
    private WorkOrderMapper workOrderMapper;

    /**
     * 查询工单信息 包含转单记录
     *
     * @param orderId 工单信息主键
     * @return 工单信息
     */
    @Override
    public WorkOrder selectWorkOrderByOrderId(Long orderId) {
        return workOrderMapper.selectWorkOrderByOrderId(orderId);
    }

    /**
     * 查询工单信息列表 不包含转单记录
     *
     * @param workOrder 工单信息
     * @return 工单信息
     */
    @Override
    public List<WorkOrder> selectWorkOrderList(WorkOrder workOrder) {
        return workOrderMapper.selectWorkOrderList(workOrder);
    }

    /**
     * 查询工单信息列表 转给我的
     *
     * @param workOrder
     * @return
     */
    @Override
    public List<WorkOrder> selectWorkOrderListTransferToMe(WorkOrder workOrder) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        List<String> temp = new ArrayList<>();
        temp.add(sysUser.getUserId().toString());
        sysUser.getRoles().forEach(role -> {
            temp.add(role.getRoleId().toString());
        });
        workOrder.setRecipients(temp);
        return workOrderMapper.selectWorkOrderListTransferToMe(workOrder);
    }

    /**
     * 新增工单信息
     *
     * @param workOrder 工单信息
     * @return 结果
     */
    @Override
    public int insertWorkOrder(WorkOrder workOrder) {
        workOrder.setCreateTime(DateUtils.getNowDate());
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        workOrder.setCreateUser(sysUser.getUserId());
        workOrder.setCreateBy(sysUser.getNickName());
        workOrder.setCreateTime(DateUtils.getNowDate());
        return workOrderMapper.insertWorkOrder(workOrder);
    }

    /**
     * 修改工单信息
     *
     * @param workOrder 工单信息
     * @return 结果
     */
    @Transactional
    @Override
    public int updateWorkOrder(WorkOrder workOrder) {
        workOrder.setUpdateTime(DateUtils.getNowDate());
        return workOrderMapper.updateWorkOrder(workOrder);
    }

    /**
     * 批量删除工单信息
     *
     * @param orderIds 需要删除的工单信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteWorkOrderByOrderIds(Long[] orderIds) {
        return workOrderMapper.deleteWorkOrderByOrderIds(orderIds);
    }

    /**
     * 删除工单信息信息
     *
     * @param orderId 工单信息主键
     * @return 结果
     */
    @Transactional
    @Override
    public int deleteWorkOrderByOrderId(Long orderId) {
        return workOrderMapper.deleteWorkOrderByOrderId(orderId);
    }

    /**
     * 转单操作
     *
     * @param workOrderTransfer
     * @return
     */
    @Override
    public int transferWorkOrder(WorkOrderTransfer workOrderTransfer) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        workOrderTransfer.setTransferTime(DateUtils.getNowDate());
        workOrderTransfer.setTransfer(sysUser.getUserId());
        return workOrderMapper.insertWorkOrderTransfer(workOrderTransfer);
    }

    /**
     * 处理操作
     *
     * @param workOrder
     * @return
     */
    @Override
    public int doDispose(WorkOrder workOrder) {
        SysUser sysUser = SecurityUtils.getLoginUser().getSysUser();
        workOrder.setDealPern(sysUser.getUserId());
        workOrder.setUpdateBy(sysUser.getNickName());
        workOrder.setUpdateTime(DateUtils.getNowDate());
        workOrder.setOrderDealStatus("1");
        return workOrderMapper.updateWorkOrder(workOrder);
    }

    /**
     * 新增工单转单记录信息
     *
     * @param workOrder 工单信息对象
     */
    public void insertWorkOrderTransfer(WorkOrder workOrder) {
        List<WorkOrderTransfer> workOrderTransferList = workOrder.getWorkOrderTransferList();
        Long orderId = workOrder.getOrderId();
        if (StringUtils.isNotNull(workOrderTransferList)) {
            List<WorkOrderTransfer> list = new ArrayList<WorkOrderTransfer>();
            for (WorkOrderTransfer workOrderTransfer : workOrderTransferList) {
                workOrderTransfer.setOrderId(orderId);
                list.add(workOrderTransfer);
            }
            if (list.size() > 0) {
                workOrderMapper.batchWorkOrderTransfer(list);
            }
        }
    }
}
