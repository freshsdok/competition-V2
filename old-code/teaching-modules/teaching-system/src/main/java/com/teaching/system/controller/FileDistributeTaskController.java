package com.teaching.system.controller;

import java.util.List;

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
import com.teaching.system.domain.FileTask;
import com.teaching.system.domain.FileTaskNotificationForm;
import com.teaching.system.domain.vo.FileTaskNotificationVo;
import com.teaching.system.service.FileTaskAudienceService;
import com.teaching.system.service.IFileTaskService;
import com.teaching.system.service.IFileTaskNotificationService;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.page.TableDataInfo;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 文件分发任务Controller
 * 
 * @author teaching
 * @date 2026-01-07
 */
@RestController
@RequestMapping("/fileDistributeTask")
public class FileDistributeTaskController extends BaseController
{
    @Autowired
    private IFileTaskService fileTaskService;

    @Autowired
    private FileTaskAudienceService fileTaskAudienceService;

    @Autowired
    private IFileTaskNotificationService fileTaskNotificationService;

    /**
     * 查询文件分发任务列表
     */
    @RequiresPermissions("system:fileDistributeTask:list")
    @GetMapping("/list")
    public TableDataInfo list(FileTask fileTask)
    {
        startPage();
        List<FileTask> list = fileTaskService.selectFileTaskList(fileTask);
        return getDataTable(list);
    }

    /**
     * 导出文件分发任务列表
     */
    @RequiresPermissions("system:fileDistributeTask:export")
    @Log(title = "文件分发任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, FileTask fileTask)
    {
        List<FileTask> list = fileTaskService.selectFileTaskList(fileTask);
        ExcelUtil<FileTask> util = new ExcelUtil<FileTask>(FileTask.class);
        util.exportExcel(response, list, "文件分发任务数据");
    }

    /**
     * 获取文件分发任务详细信息
     */
    @RequiresPermissions("system:fileDistributeTask:query")
    @GetMapping(value = "/getDetail/{id}")
    public AjaxResult getInfo(@PathVariable("id") Long id)
    {
        return success(fileTaskService.selectFileTaskById(id));
    }

    /**
     * 新增文件分发任务
     */
    @RequiresPermissions("system:fileDistributeTask:add")
    @Log(title = "文件分发任务", businessType = BusinessType.INSERT)
    @PostMapping("/saveFileTask")
    public AjaxResult add(@RequestBody FileTask fileTask)
    {
        return toAjax(fileTaskService.insertFileTask(fileTask));
    }

    /**
     * 修改文件分发任务
     */
    @RequiresPermissions("system:fileDistributeTask:edit")
    @Log(title = "文件分发任务", businessType = BusinessType.UPDATE)
    @PostMapping("/editFileTask")
    public AjaxResult edit(@RequestBody FileTask fileTask)
    {
        return toAjax(fileTaskService.updateFileTask(fileTask));
    }


    @RequiresPermissions("system:fileDistributeTask:edit")
    @Log(title = "修改文件分发任务状态", businessType = BusinessType.UPDATE)
    @PostMapping("/updateTaskStatus")
    public AjaxResult updateTaskStatus(@RequestBody FileTask fileTask) {
        return toAjax(fileTaskService.updateFileTaskStatus(fileTask));
    }
    /**
     * 删除文件分发任务
     */
    @RequiresPermissions("system:fileDistributeTask:remove")
    @Log(title = "文件分发任务", businessType = BusinessType.DELETE)
	@GetMapping("/remove/{ids}")
    public AjaxResult remove(@PathVariable Long[] ids)
    {
        return toAjax(fileTaskService.deleteFileTaskByIds(ids));
    }

    /**
     * 查看上传任务动态应交人员及当前上传状态。
     */
    @RequiresPermissions("system:fileDistributeTask:list")
    @GetMapping("/{taskId}/recipients")
    public AjaxResult recipients(@PathVariable Long taskId,
                                 @RequestParam(defaultValue = "1") int pageNum,
                                 @RequestParam(defaultValue = "10") int pageSize,
                                 @RequestParam(required = false) String keyword,
                                 @RequestParam(defaultValue = "ALL") String uploadStatus) {
        return success(fileTaskAudienceService.getRecipients(
                taskId, pageNum, pageSize, keyword, uploadStatus));
    }

    /**
     * 发送任务通知。服务端始终依据完整实时受众重新计算收件人。
     */
    @RequiresPermissions("system:fileDistributeTask:notify")
    @Log(title = "发送文件任务通知", businessType = BusinessType.INSERT)
    @PostMapping("/{taskId}/notifications")
    public AjaxResult sendNotification(@PathVariable Long taskId,
                                       @RequestBody FileTaskNotificationForm form) {
        return success(fileTaskNotificationService.send(taskId, form));
    }

    /**
     * 管理端分页查看该任务全部通知历史（含已撤回）。
     */
    @RequiresPermissions("system:fileDistributeTask:list")
    @GetMapping("/{taskId}/notifications")
    public TableDataInfo notificationList(@PathVariable Long taskId) {
        fileTaskAudienceService.requireUploadTask(taskId);
        startPage();
        List<FileTaskNotificationVo> list =
                fileTaskNotificationService.selectAdminList(taskId);
        return getDataTable(list);
    }

    /**
     * 管理端查看通知正文，不返回收件人 ID 快照。
     */
    @RequiresPermissions("system:fileDistributeTask:list")
    @GetMapping("/{taskId}/notifications/{notificationId}")
    public AjaxResult notificationDetail(@PathVariable Long taskId,
                                         @PathVariable Long notificationId) {
        return success(fileTaskNotificationService.selectAdminDetail(taskId, notificationId));
    }

    /**
     * 幂等撤回整条通知。
     */
    @RequiresPermissions("system:fileDistributeTask:notify")
    @Log(title = "撤回文件任务通知", businessType = BusinessType.UPDATE)
    @PutMapping("/{taskId}/notifications/{notificationId}/withdraw")
    public AjaxResult withdrawNotification(@PathVariable Long taskId,
                                           @PathVariable Long notificationId) {
        fileTaskNotificationService.withdraw(taskId, notificationId);
        return success();
    }

    // 统计接口
//    @GetMapping("/count")
//    public AjaxResult count() {
//        return success(fileTaskService.count());
//    }
}
