package com.teaching.system.controller;

import java.util.Date;
import java.util.List;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.api.domain.SysAuditLog;
import com.teaching.system.service.ISysAuditLogService;

/**
 * 审计日志记录
 * 
 * @author teaching
 */
@RestController
@RequestMapping("/auditlog")
public class SysAuditLogController extends BaseController
{
    @Autowired
    private ISysAuditLogService auditLogService;

    /**
     * 获取审计日志列表
     */
    @RequiresPermissions("system:auditlog:list")
    @GetMapping("/list")
    public TableDataInfo list(SysAuditLog auditLog)
    {
        startPage();
        List<SysAuditLog> list = auditLogService.selectAuditLogList(auditLog);
        return getDataTable(list);
    }

    /**
     * 根据审计日志编号获取详细信息
     */
    @RequiresPermissions("system:auditlog:query")
    @GetMapping(value = "/{auditId}")
    public AjaxResult getInfo(@PathVariable Long auditId)
    {
        return success(auditLogService.selectAuditLogById(auditId));
    }

    /**
     * 导出审计日志列表
     */
    @Log(title = "审计日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:auditlog:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysAuditLog auditLog)
    {
        List<SysAuditLog> list = auditLogService.selectAuditLogList(auditLog);
        ExcelUtil<SysAuditLog> util = new ExcelUtil<SysAuditLog>(SysAuditLog.class);
        util.exportExcel(response, list, "审计日志");
    }

    /**
     * 审计日志（标记为已审计）
     */
    @RequiresPermissions("system:auditlog:edit")
    @Log(title = "审计日志", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult audit(@RequestBody SysAuditLog auditLog)
    {
        auditLog.setAuditStatus("1"); // 已审计
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditTime(new Date());
        return toAjax(auditLogService.updateAuditLog(auditLog));
    }

    /**
     * 忽略审计日志
     */
    @RequiresPermissions("system:auditlog:edit")
    @Log(title = "审计日志", businessType = BusinessType.UPDATE)
    @PutMapping("/ignore")
    public AjaxResult ignore(@RequestBody SysAuditLog auditLog)
    {
        auditLog.setAuditStatus("2"); // 已忽略
        auditLog.setAuditBy(SecurityUtils.getUsername());
        auditLog.setAuditTime(new Date());
        return toAjax(auditLogService.updateAuditLog(auditLog));
    }

    /**
     * 删除审计日志
     */
    @RequiresPermissions("system:auditlog:remove")
    @Log(title = "审计日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{auditIds}")
    public AjaxResult remove(@PathVariable Long[] auditIds)
    {
        return toAjax(auditLogService.deleteAuditLogByIds(auditIds));
    }

    /**
     * 清空审计日志
     */
    @RequiresPermissions("system:auditlog:remove")
    @Log(title = "审计日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        auditLogService.cleanAuditLog();
        return success();
    }

    /**
     * 统计审计日志（按审计类型）
     */
    @RequiresPermissions("system:auditlog:list")
    @GetMapping("/statistics/byType")
    public AjaxResult statisticsByType()
    {
        return success(auditLogService.countByAuditType());
    }

    /**
     * 统计审计日志（按风险级别）
     */
    @RequiresPermissions("system:auditlog:list")
    @GetMapping("/statistics/byRiskLevel")
    public AjaxResult statisticsByRiskLevel()
    {
        return success(auditLogService.countByRiskLevel());
    }

    /**
     * 统计异常行为
     */
    @RequiresPermissions("system:auditlog:list")
    @GetMapping("/statistics/abnormalBehavior")
    public AjaxResult statisticsAbnormalBehavior()
    {
        return success(auditLogService.countAbnormalBehavior());
    }

    /**
     * 新增审计日志（内部调用）
     */
    @InnerAuth
    @PostMapping
    public AjaxResult add(@RequestBody SysAuditLog auditLog)
    {
        return toAjax(auditLogService.insertAuditLog(auditLog));
    }
}
