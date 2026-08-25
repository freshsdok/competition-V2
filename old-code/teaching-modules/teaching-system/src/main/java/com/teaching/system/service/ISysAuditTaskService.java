package com.teaching.system.service;

import com.teaching.common.core.web.page.TableDataInfo;
import com.teaching.system.api.domain.AuthInfo;
import com.teaching.system.domain.SysAuditTask;
import com.teaching.system.domain.SysAuditTaskSubinfo;

import java.util.List;

/**
 * 审核任务Service接口
 *
 * @author teaching
 * @date 2025-10-16
 */
public interface ISysAuditTaskService {
    /**
     * 查询审核任务
     *
     * @param taskId 审核任务主键
     * @return 审核任务
     */
    public SysAuditTask selectSysAuditTaskByTaskId(Long taskId);

    /**
     * 仅返回图片路径
     * @param taskId
     * @return
     */
    public String selectSysAuditTaskPicByTaskId(Long taskId);

    /**
     * 查询审核任务列表
     * 进行中的
     *
     * @param sysAuditTask 审核任务
     * @return 审核任务集合
     */
    public List<SysAuditTask> selectSysAuditTaskList(SysAuditTask sysAuditTask);

    /**
     * 查询审核任务列表
     * @param sysAuditTask
     * @return
     */
    public TableDataInfo selectSysAuditTaskListPage(SysAuditTask sysAuditTask);

    /**
     * 查询审核任务列表
     * 已完成的
     *
     * @param sysAuditTask
     * @return
     */
    public List<SysAuditTask> selectSysAuditTaskFinishList(SysAuditTask sysAuditTask);

    /**
     * 查询审核任务列表
     * 已完成的
     * @param sysAuditTask
     * @return
     */
    public TableDataInfo selectSysAuditTaskFinishListPage(SysAuditTask sysAuditTask);
    /**
     * 新增审核任务
     *
     * @param sysAuditTask 审核任务
     * @return 结果
     */
    public int insertSysAuditTask(SysAuditTask sysAuditTask);

    /**
     * 身份认证  新增审核
     * @param sysAuditTask
     * @return
     */
    public String authUserInsertSysAuditTask(SysAuditTask sysAuditTask);

    /**
     * 实名认证新增审核（认证类型不包含身份证）
     * @param authInfo
     * @return
     */
    public int realNameAuthAuditTask(AuthInfo authInfo) throws Exception;

    /**
     * 审核
     *
     * @param subInfo 审核任务
     * @return 结果
     */
    public int sysAuditTaskDoAudit(SysAuditTaskSubinfo subInfo);

    /**
     * 批量审核
     * @param subInfos
     * @return
     */
    public int sysAuditTaskDoAudits(List<SysAuditTaskSubinfo> subInfos);

    /**
     * 章节视频审核
     * @param subInfo
     * @return
     */
    public int sysAuditTaskVideoDoAudit(SysAuditTaskSubinfo subInfo);

    /**
     * 修改审核任务
     *
     * @param sysAuditTask 审核任务
     * @return 结果
     */
    public int updateSysAuditTask(SysAuditTask sysAuditTask);

    /**
     * 批量删除审核任务
     *
     * @param taskIds 需要删除的审核任务主键集合
     * @return 结果
     */
    public int deleteSysAuditTaskByTaskIds(Long[] taskIds);

    /**
     * 删除审核任务信息
     *
     * @param taskId 审核任务主键
     * @return 结果
     */
    public int deleteSysAuditTaskByTaskId(Long taskId);

    /**
     * 根据审核类型获取审核拒绝意见
     * @param auditType 审核类型
     * @param businessId 业务id
     * @return 拒绝意见
     */
    public String getCheckOpinion(String auditType,Long businessId);
}
