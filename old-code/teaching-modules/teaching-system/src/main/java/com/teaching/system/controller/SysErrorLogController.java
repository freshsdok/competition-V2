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
import com.teaching.system.api.domain.SysErrorLog;
import com.teaching.system.service.ISysErrorLogService;

/**
 * 错误日志记录
 * 
 * @author teaching
 */
@RestController
@RequestMapping("/errorlog")
public class SysErrorLogController extends BaseController
{
    @Autowired
    private ISysErrorLogService errorLogService;

    /**
     * 获取错误日志列表
     */
    @RequiresPermissions("system:errorlog:list")
    @GetMapping("/list")
    public TableDataInfo list(SysErrorLog errorLog)
    {
        startPage();
        List<SysErrorLog> list = errorLogService.selectErrorLogList(errorLog);
        return getDataTable(list);
    }

    /**
     * 根据错误日志编号获取详细信息
     */
    @RequiresPermissions("system:errorlog:query")
    @GetMapping(value = "/{errorId}")
    public AjaxResult getInfo(@PathVariable Long errorId)
    {
        return success(errorLogService.selectErrorLogById(errorId));
    }

    /**
     * 导出错误日志列表
     */
    @Log(title = "错误日志", businessType = BusinessType.EXPORT)
    @RequiresPermissions("system:errorlog:export")
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysErrorLog errorLog)
    {
        List<SysErrorLog> list = errorLogService.selectErrorLogList(errorLog);
        ExcelUtil<SysErrorLog> util = new ExcelUtil<SysErrorLog>(SysErrorLog.class);
        util.exportExcel(response, list, "错误日志");
    }

    /**
     * 处理错误日志（标记为已处理）
     */
    @RequiresPermissions("system:errorlog:edit")
    @Log(title = "错误日志", businessType = BusinessType.UPDATE)
    @PutMapping("/handle")
    public AjaxResult handle(@RequestBody SysErrorLog errorLog)
    {
        errorLog.setStatus("1"); // 已处理
        errorLog.setHandleBy(SecurityUtils.getUsername());
        errorLog.setHandleTime(new Date());
        return toAjax(errorLogService.updateErrorLog(errorLog));
    }

    /**
     * 忽略错误日志
     */
    @RequiresPermissions("system:errorlog:edit")
    @Log(title = "错误日志", businessType = BusinessType.UPDATE)
    @PutMapping("/ignore")
    public AjaxResult ignore(@RequestBody SysErrorLog errorLog)
    {
        errorLog.setStatus("2"); // 已忽略
        errorLog.setHandleBy(SecurityUtils.getUsername());
        errorLog.setHandleTime(new Date());
        return toAjax(errorLogService.updateErrorLog(errorLog));
    }

    /**
     * 删除错误日志
     */
    @RequiresPermissions("system:errorlog:remove")
    @Log(title = "错误日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{errorIds}")
    public AjaxResult remove(@PathVariable Long[] errorIds)
    {
        return toAjax(errorLogService.deleteErrorLogByIds(errorIds));
    }

    /**
     * 清空错误日志
     */
    @RequiresPermissions("system:errorlog:remove")
    @Log(title = "错误日志", businessType = BusinessType.CLEAN)
    @DeleteMapping("/clean")
    public AjaxResult clean()
    {
        errorLogService.cleanErrorLog();
        return success();
    }

    /**
     * 新增错误日志（内部调用）
     */
    @InnerAuth
    @PostMapping
    public AjaxResult add(@RequestBody SysErrorLog errorLog)
    {
        return toAjax(errorLogService.insertErrorLog(errorLog));
    }
}
