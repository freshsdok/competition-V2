package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.common.security.utils.SecurityUtils;
import com.teaching.system.domain.FileDownloadRecord;
import com.teaching.system.service.IFileDownloadRecordService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.teaching.common.security.utils.SecurityUtils.getUsername;

/**
 * 文件下载记录Controller
 *
 * @author teaching
 * @date 2026-01-09
 */
@RestController
@RequestMapping("/downLoadRecord")
public class FileDownloadRecordController extends BaseController {
    @Autowired
    private IFileDownloadRecordService fileDownloadRecordService;

    /**
     * 查询文件下载记录列表
     */
    @RequiresPermissions("system:downRecord:list")
    @GetMapping("/list")
    public TableDataInfo list(FileDownloadRecord fileDownloadRecord) {
        startPage();
        List<FileDownloadRecord> list = fileDownloadRecordService.selectFileDownloadRecordList(fileDownloadRecord);
        return getDataTable(list);
    }

    /**
     * 导出文件下载记录列表
     */
    @RequiresPermissions("system:downRecord:export")
    @Log(title = "文件下载记录", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FileDownloadRecord fileDownloadRecord) {
        List<FileDownloadRecord> list = fileDownloadRecordService.selectFileDownloadRecordList(fileDownloadRecord);
        ExcelUtil<FileDownloadRecord> util = new ExcelUtil<FileDownloadRecord>(FileDownloadRecord.class);
        util.exportExcel(response, list, "文件下载记录数据");
    }

    /**
     * 获取文件下载记录详细信息
     */
    @RequiresPermissions("system:downRecord:query")
    @GetMapping(value = "/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id) {
        return success(fileDownloadRecordService.selectFileDownloadRecordById(id));
    }

    /**
     * 新增文件下载记录
     * 传值  fileTaskId任务id，fileTaskName任务名称，fileName文件名称
     */
    @Log(title = "文件下载记录", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody FileDownloadRecord fileDownloadRecord) {
        fileDownloadRecord.setCreateBy(getUsername());
        fileDownloadRecord.setUserId(SecurityUtils.getUserId());
        fileDownloadRecord.setUserName(SecurityUtils.getUsername());
        return toAjax(fileDownloadRecordService.insertFileDownloadRecord(fileDownloadRecord));
    }

    /**
     * 修改文件下载记录
     */
    @RequiresPermissions("system:downRecord:edit")
    @Log(title = "文件下载记录", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody FileDownloadRecord fileDownloadRecord) {
        return toAjax(fileDownloadRecordService.updateFileDownloadRecord(fileDownloadRecord));
    }

    /**
     * 删除文件下载记录
     */
    @RequiresPermissions("system:downRecord:remove")
    @Log(title = "文件下载记录", businessType = BusinessType.DELETE)
    @DeleteMapping("/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids) {
        return toAjax(fileDownloadRecordService.deleteFileDownloadRecordByIds(ids));
    }
}
