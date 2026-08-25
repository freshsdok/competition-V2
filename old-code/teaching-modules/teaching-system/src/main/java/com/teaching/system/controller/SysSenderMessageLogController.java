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
import com.teaching.system.domain.SysSenderMessageLog;
import com.teaching.system.service.ISysSenderMessageLogService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;

/**
 * 推送信息日志Controller
 * 
 * @author teaching
 * @date 2026-01-30
 */
@RestController
@RequestMapping("/sysSenderMessageLog")
public class SysSenderMessageLogController extends BaseController
{
    @Autowired
    private ISysSenderMessageLogService sysSenderMessageLogService;

    /**
     * 查询推送信息日志列表
     */
    @RequiresPermissions("system:sysSenderMessageLog:list")
    @GetMapping("/list")
    public TableDataInfo list(SysSenderMessageLog sysSenderMessageLog)
    {
        startPage();
        List<SysSenderMessageLog> list = sysSenderMessageLogService.selectSysSenderMessageLogList(sysSenderMessageLog);
        return getDataTable(list);
    }

    /**
     * 导出推送信息日志列表
     */
    @RequiresPermissions("system:sysSenderMessageLog:export")
    @Log(title = "推送信息日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysSenderMessageLog sysSenderMessageLog)
    {
        List<SysSenderMessageLog> list = sysSenderMessageLogService.selectSysSenderMessageLogList(sysSenderMessageLog);
        ExcelUtil<SysSenderMessageLog> util = new ExcelUtil<SysSenderMessageLog>(SysSenderMessageLog.class);
        util.exportExcel(response, list, "推送信息日志数据");
    }

    /**
     * 获取推送信息日志详细信息
     */
    @RequiresPermissions("system:sysSenderMessageLog:query")
    @GetMapping(value = "/getSysSenderMessageLogBySendId/{sendId}")
    public AjaxResult getInfo(@PathVariable("sendId") Long sendId)
    {
        return success(sysSenderMessageLogService.selectSysSenderMessageLogBySendId(sendId));
    }

    /**
     * 新增推送信息日志
     */
    @RequiresPermissions("system:sysSenderMessageLog:add")
    @Log(title = "推送信息日志", businessType = BusinessType.INSERT)
    @PostMapping("/saveSysSenderMessageLog")
    public AjaxResult add(@RequestBody SysSenderMessageLog sysSenderMessageLog)
    {
        return toAjax(sysSenderMessageLogService.insertSysSenderMessageLog(sysSenderMessageLog));
    }

    /**
     * 修改推送信息日志
     */
    @RequiresPermissions("system:sysSenderMessageLog:edit")
    @Log(title = "推送信息日志", businessType = BusinessType.UPDATE)
    @PostMapping("/updateSysSenderMessageLog")
    public AjaxResult edit(@RequestBody SysSenderMessageLog sysSenderMessageLog)
    {
        return toAjax(sysSenderMessageLogService.updateSysSenderMessageLog(sysSenderMessageLog));
    }

    /**
     * 删除推送信息日志
     */
    @RequiresPermissions("system:sysSenderMessageLog:remove")
    @Log(title = "推送信息日志", businessType = BusinessType.DELETE)
	@GetMapping("/deleteSysSenderMessageLog/{sendIds}")
    public AjaxResult remove(@PathVariable Long[] sendIds)
    {
        return toAjax(sysSenderMessageLogService.deleteSysSenderMessageLogBySendIds(sendIds));
    }
}
