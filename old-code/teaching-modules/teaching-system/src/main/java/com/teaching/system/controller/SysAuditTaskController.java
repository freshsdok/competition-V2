package com.teaching.system.controller;

import com.teaching.common.core.utils.poi.ExcelUtil;
import com.teaching.common.core.web.controller.BaseController;
import com.teaching.common.core.web.domain.AjaxResult;
import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.common.log.annotation.Log;
import com.teaching.common.log.enums.BusinessType;
import com.teaching.common.security.annotation.InnerAuth;
import com.teaching.common.security.annotation.Logical;
import com.teaching.common.security.annotation.RequiresPermissions;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.domain.SysAuditTask;
import com.teaching.system.domain.SysAuditTaskSubinfo;
import com.teaching.system.service.ISysAuditTaskService;
import com.teaching.system.service.ISysUserService;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 审核任务Controller
 *
 * @author teaching
 * @date 2025-10-16
 */
@RestController
@RequestMapping("/task")
public class SysAuditTaskController extends BaseController {
    @Autowired
    private ISysAuditTaskService sysAuditTaskService;
    @Autowired
    private ISysUserService sysUserService;

    /**
     * 查询审核任务列表 进行中的
     */
    @RequiresPermissions(value = {"system:task:list", "page:task:list", "race:task:list", "course:task:list",
            "team:task:list", "student:task:list", "teacher:task:list", "school:task:list", "enterprise:task:list", "info:task:list", "apply:task:list", "notice:task:list",
            "chapterVideo:task:list", "realName:task:list", "raceTrack:task:list"}, logical = Logical.OR)
    @GetMapping("/list")
    public TableDataInfo list(SysAuditTask sysAuditTask) {
        /*startPage();
        List<SysAuditTask> list = sysAuditTaskService.selectSysAuditTaskList(sysAuditTask);
        return getDataTable(list);*/
        return sysAuditTaskService.selectSysAuditTaskListPage(sysAuditTask);
    }

    /**
     * 查询审核任务列表 已完成的
     */
    @RequiresPermissions(value = {"system:task:list", "page:task:list", "race:task:list", "course:task:list",
            "team:task:list", "student:task:list", "teacher:task:list", "school:task:list", "enterprise:task:list", "info:task:list", "apply:task:list", "notice:task:list",
            "chapterVideo:task:list", "realName:task:list", "raceTrack:task:list"}, logical = Logical.OR)
    @GetMapping("/finish")
    public TableDataInfo finishList(SysAuditTask sysAuditTask) {
        /*startPage();
        List<SysAuditTask> list = sysAuditTaskService.selectSysAuditTaskFinishList(sysAuditTask);
        return getDataTable(list);*/
        return sysAuditTaskService.selectSysAuditTaskFinishListPage(sysAuditTask);
    }

    /**
     * 导出审核任务列表
     */
    @RequiresPermissions("system:task:export")
    @Log(title = "审核任务", businessType = BusinessType.EXPORT)
    @PostMapping("/export")
    public void export(HttpServletResponse response, SysAuditTask sysAuditTask) {
        List<SysAuditTask> list = sysAuditTaskService.selectSysAuditTaskList(sysAuditTask);
        ExcelUtil<SysAuditTask> util = new ExcelUtil<SysAuditTask>(SysAuditTask.class);
        util.exportExcel(response, list, "审核任务数据");
    }

    /**
     * 获取审核任务详细信息
     */
    @RequiresPermissions(value = {"system:task:audit", "page:task:audit", "race:task:audit", "course:task:audit",
            "team:task:audit", "student:task:audit", "teacher:task:audit", "school:task:audit", "enterprise:task:audit", "info:task:audit", "apply:task:audit", "notice:task:audit",
            "chapterVideo:task:audit", "realName:task:audit", "raceTrack:task:audit"}, logical = Logical.OR)
    @GetMapping(value = "/{taskId}")
    public AjaxResult getInfo(@PathVariable("taskId") Long taskId) {
        return success(sysAuditTaskService.selectSysAuditTaskByTaskId(taskId));
    }

    /**
     * 教师审核仅返回上传的证件照图片路径
     *
     * @param taskId
     * @return
     */
    @GetMapping(value = "/pic/{taskId}")
    public AjaxResult getPicInfo(@PathVariable("taskId") Long taskId) {
        return AjaxResult.success("查询成功", sysAuditTaskService.selectSysAuditTaskPicByTaskId(taskId));
    }

    /**
     * 新增审核任务  business 发起审核
     * 前端传值auditType和businessId
     */
    @RequiresPermissions(value = {"page:task:submit", "race:task:submit", "course:task:submit", "team:task:submit",
            "info:task:submit", "apply:task:submit", "notice:task:submit",
            "chapterVideo:task:submit", "raceTrack:task:submit"}, logical = Logical.OR)
    @Log(title = "审核任务", businessType = BusinessType.INSERT)
    @PostMapping
    public AjaxResult add(@RequestBody SysAuditTask sysAuditTask) {
        return toAjax(sysAuditTaskService.insertSysAuditTask(sysAuditTask));
    }

    /**
     * 实名认证新增审核任务 不包含身份证类型认证
     * 传值：realName="真实姓名",idCard="证件号",idCardType="",idCardFornt="正面照片",idCardContrary="反面照片"
     *
     * @param authInfo
     * @return
     */
    @PostMapping("/pc/realName")
    public AjaxResult pcRealName(@RequestBody AuthInfo authInfo) throws Exception {
        return toAjax(sysAuditTaskService.realNameAuthAuditTask(authInfo));
    }

    /**
     * 根据审核类型获取审核意见
     *
     * @param auditType  审核类型
     * @param businessId 业务id
     * @return 拒绝意见
     */
    @InnerAuth
    @GetMapping("/getCheckOpinion/{auditType}/{businessId}")
    public AjaxResult getCheckOpinion(@PathVariable String auditType, @PathVariable Long businessId) {
        String checkOpinion = sysAuditTaskService.getCheckOpinion(auditType, businessId);
        return AjaxResult.success("查询成功", checkOpinion);
    }

    /**
     * 审核操作
     * 前端传值 taskId任务id、auditConfigId流程节点id、checkOpinion审核意见、checkStatus审核状态(4已通过，5已拒绝)
     */
    @RequiresPermissions(value = {"system:task:audit", "page:task:audit", "race:task:audit", "course:task:audit",
            "team:task:audit", "student:task:audit", "teacher:task:audit", "school:task:audit", "enterprise:task:audit", "info:task:audit", "apply:task:audit", "notice:task:audit", "realName:task:audit"}, logical = Logical.OR)
    @Log(title = "审核任务", businessType = BusinessType.UPDATE)
    @PutMapping("/audit")
    public AjaxResult doAudit(@Validated @RequestBody SysAuditTaskSubinfo subInfo) {
        return toAjax(sysAuditTaskService.sysAuditTaskDoAudit(subInfo));
    }

    /**
     * 批量审核操作
     *
     * @param subInfos
     * @return
     */
    @PutMapping("/audits")
    public AjaxResult doAudits(@RequestBody List<SysAuditTaskSubinfo> subInfos) {
        return toAjax(sysAuditTaskService.sysAuditTaskDoAudits(subInfos));
    }

    /**
     * 视频审核操作
     * 前端传值 taskId任务id、auditConfigId流程节点id、ChapterAuditResult（章节及各个视频是id、审核状态(4已通过，5已拒绝)、审核意见）
     */
    @RequiresPermissions(value = {"system:task:audit", "chapterVideo:task:audit"}, logical = Logical.OR)
    @Log(title = "审核任务", businessType = BusinessType.UPDATE)
    @PutMapping("/videoAudit")
    public AjaxResult sysAuditTaskVideoDoAudit(@RequestBody SysAuditTaskSubinfo subInfo) {
        return toAjax(sysAuditTaskService.sysAuditTaskVideoDoAudit(subInfo));
    }

    /**
     * 修改审核任务
     */
    @RequiresPermissions("system:task:edit")
    @Log(title = "审核任务", businessType = BusinessType.UPDATE)
    @PutMapping
    public AjaxResult edit(@RequestBody SysAuditTask sysAuditTask) {
        return toAjax(sysAuditTaskService.updateSysAuditTask(sysAuditTask));
    }

    /**
     * 删除审核任务
     */
    @RequiresPermissions("system:task:remove")
    @Log(title = "审核任务", businessType = BusinessType.DELETE)
    @DeleteMapping("/{taskIds}")
    public AjaxResult remove(@PathVariable Long[] taskIds) {
        return toAjax(sysAuditTaskService.deleteSysAuditTaskByTaskIds(taskIds));
    }

    /**
     * 新增审核任务  内部调用接口
     *
     * @param auditType
     * @param businessId
     * @return
     */
    @InnerAuth
    @GetMapping("/addAuditTask/{auditType}/{businessId}")
    public AjaxResult addAuditTask(@PathVariable String auditType, @PathVariable Long businessId) {
        SysAuditTask sysAuditTask = new SysAuditTask(businessId, auditType);
        return success(sysAuditTaskService.insertSysAuditTask(sysAuditTask));
    }

    /**
     * 获取提交人 根据权限字符串
     *
     * @param type 审核类型
     * @return 提交人列表
     */
    @GetMapping("/getUser/{type}")
    public AjaxResult getSubmitAndAuditUser(@PathVariable String type) {
        return success(sysUserService.getSubmitUser(type));
    }
}
