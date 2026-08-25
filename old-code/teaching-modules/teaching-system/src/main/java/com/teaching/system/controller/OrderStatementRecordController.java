package com.teaching.system.controller;

import java.util.List;
import java.io.IOException;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.domain.OrderStatementRecord;
import com.teaching.system.service.IOrderStatementRecordService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 对账单对账记录Controller
 * 
 * @author teaching
 * @date 2025-10-27
 */
@RestController
@RequestMapping("/record")
public class OrderStatementRecordController extends BaseController
{
    @Autowired
    private IOrderStatementRecordService orderStatementRecordService;

    /**
     * 查询对账单对账记录列表
     */
    @RequiresPermissions("system:record:list")
    @GetMapping("/list")
    public TableDataInfo list(OrderStatementRecord orderStatementRecord)
    {
        startPage();
        List<OrderStatementRecord> list = orderStatementRecordService.selectOrderStatementRecordList(orderStatementRecord);
        return getDataTable(list);
    }

    /**
     * 导出对账单对账记录列表
     */
    @RequiresPermissions("system:record:export")
    @Log(title = "对账单对账记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, OrderStatementRecord orderStatementRecord)
    {
        List<OrderStatementRecord> list = orderStatementRecordService.selectOrderStatementRecordList(orderStatementRecord);
        ExcelUtil<OrderStatementRecord> util = new ExcelUtil<OrderStatementRecord>(OrderStatementRecord.class);
        util.exportExcel(response, list, "对账单对账记录数据");
    }

    /**
     * 获取对账单对账记录详细信息
     */
    @RequiresPermissions("system:record:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(orderStatementRecordService.selectOrderStatementRecordById(id));
    }

    /**
     * 新增对账单对账记录
     */
    @RequiresPermissions("system:record:add")
    @Log(title = "对账单对账记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody OrderStatementRecord orderStatementRecord)
    {
        return toAjax(orderStatementRecordService.insertOrderStatementRecord(orderStatementRecord));
    }

    /**
     * 修改对账单对账记录
     */
    @RequiresPermissions("system:record:edit")
    @Log(title = "对账单对账记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody OrderStatementRecord orderStatementRecord)
    {
        return toAjax(orderStatementRecordService.updateOrderStatementRecord(orderStatementRecord));
    }

    /**
     * 删除对账单对账记录
     */
    @RequiresPermissions("system:record:remove")
    @Log(title = "对账单对账记录", businessType = BusinessType.DELETE)
	@DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(orderStatementRecordService.deleteOrderStatementRecordByIds(ids));
    }
}
