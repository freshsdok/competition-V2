package com.teaching.competition.controller;


import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.competition.service.IChangeLogService;
import com.teaching.system.api.domain.ChangeLog;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

/**
 * 参赛信息变动日志Controller
 *
 * @author teaching
 * @date 2026-01-28
 */
@RestController
@RequestMapping("/log")
public class ChangeLogController extends BaseController {
    @Autowired
    private IChangeLogService changeLogService;

    /**
     * 查询参赛信息变动日志列表
     */
    @RequiresPermissions("competition:log:list")
    @GetMapping("/list")
    public TableDataInfo list(ChangeLog changeLog) {
        startPage();
        List<ChangeLog> list = changeLogService.selectChangeLogList(changeLog);
        return getDataTable(list);
    }

    /**
     * 导出参赛信息变动日志列表
     */
    @RequiresPermissions("competition:log:export")
    @Log(title = "参赛信息变动日志", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, ChangeLog changeLog) {
        List<ChangeLog> list = new ArrayList<>();
        if("all".equals(changeLog.getExportType())){
            list = changeLogService.selectChangeLogList(new ChangeLog());
        }else{
            list = changeLogService.selectChangeLogList(changeLog);
        }
        ExcelUtil<ChangeLog> util = new ExcelUtil<ChangeLog>(ChangeLog.class);
        util.exportExcel(response, list, "参赛信息变动日志数据");
    }

    /**
     * 获取参赛信息变动日志详细信息
     */
    @RequiresPermissions("competition:log:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(changeLogService.selectChangeLogById(id));
    }

    /**
     * 新增参赛信息变动日志
     */
    @RequiresPermissions("competition:log:add")
    @Log(title = "参赛信息变动日志", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody ChangeLog changeLog) {
        return toAjax(changeLogService.insertChangeLog(changeLog));
    }

    @InnerAuth
    @PostMapping("/innerAdd")
    public AjaxResult innerAdd(@RequestBody ChangeLog changeLog) {
        return toAjax(changeLogService.insertChangeLog(changeLog));
    }

    /**
     * 修改参赛信息变动日志
     */
    @RequiresPermissions("competition:log:edit")
    @Log(title = "参赛信息变动日志", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody ChangeLog changeLog) {
        return toAjax(changeLogService.updateChangeLog(changeLog));
    }

    /**
     * 删除参赛信息变动日志
     */
    @RequiresPermissions("competition:log:remove")
    @Log(title = "参赛信息变动日志", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(changeLogService.deleteChangeLogByIds(ids));
    }
}
