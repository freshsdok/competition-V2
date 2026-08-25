package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.WorkOrder;
import com.teaching.system.domain.WorkOrderTransfer;
import com.teaching.system.service.IWorkOrderService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 工单信息Controller
 *
 * @author teaching
 * @date 2025-10-30
 */
@RestController
@RequestMapping("/workOrder")
public class WorkOrderController extends BaseController {
    @Autowired
    private IWorkOrderService workOrderService;

    /**
     * 查询工单信息列表  我发起的
     */
    @RequiresPermissions("system:workOrder:list")
    @GetMapping("/list")
    public TableDataInfo list(WorkOrder workOrder) {
        Long userId = SecurityUtils.getLoginUser().getSysUser().getUserId();
        workOrder.setCreateUser(userId);
        startPage();
        List<WorkOrder> list = workOrderService.selectWorkOrderList(workOrder);
        return getDataTable(list);
    }

    /**
     * 查询工单信息列表  转给我的
     */
    @RequiresPermissions("system:workOrder:list")
    @GetMapping("/transferList")
    public TableDataInfo transferToMe(WorkOrder workOrder) {
        startPage();
        List<WorkOrder> list = workOrderService.selectWorkOrderListTransferToMe(workOrder);
        return getDataTable(list);
    }

    /**
     * 导出工单信息列表
     */
    @RequiresPermissions("system:workOrder:export")
    @Log(title = "工单信息", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, WorkOrder workOrder) {
        List<WorkOrder> list = workOrderService.selectWorkOrderList(workOrder);
        ExcelUtil<WorkOrder> util = new ExcelUtil<WorkOrder>(WorkOrder.class);
        util.exportExcel(response, list, "工单信息数据");
    }

    /**
     * 获取工单信息详细信息 包含转单记录
     */
    @RequiresPermissions("system:workOrder:query")
    @GetMapping(value = "/{orderId}")
    public AjaxResult getInfo(@PathVariable("orderId") Long orderId) {
        return success(workOrderService.selectWorkOrderByOrderId(orderId));
    }

    /**
     * 新增工单信息
     */
    @RequiresPermissions("system:workOrder:add")
    @Log(title = "工单信息", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody WorkOrder workOrder) {
        return toAjax(workOrderService.insertWorkOrder(workOrder));
    }

    /**
     * 修改工单信息
     */
    @RequiresPermissions("system:workOrder:edit")
    @Log(title = "工单信息", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody WorkOrder workOrder) {
        return toAjax(workOrderService.updateWorkOrder(workOrder));
    }

    /**
     * 删除工单信息
     */
    @RequiresPermissions("system:workOrder:remove")
    @Log(title = "工单信息", businessType = BusinessType.DELETE)
    @DeleteMapping("/{orderIds}")
    public AjaxResult remove(@PathVariable Long[] orderIds) {
        return toAjax(workOrderService.deleteWorkOrderByOrderIds(orderIds));
    }

    /**
     * 转单操作
     *
     * @param workOrderTransfer
     * @return
     */
    @RequiresPermissions("system:workOrder:query")
    @PostMapping(value = "/transferOrder")
    public AjaxResult transferOrder(@RequestBody WorkOrderTransfer workOrderTransfer) {
        return success(workOrderService.transferWorkOrder(workOrderTransfer));
    }

    /**
     * 处理操作
     *
     * @param workOrder
     * @return
     */
    @RequiresPermissions("system:workOrder:query")
    @PostMapping(value = "/dispose")
    public AjaxResult doDispose(@RequestBody WorkOrder workOrder) {
        return success(workOrderService.doDispose(workOrder));
    }
}
