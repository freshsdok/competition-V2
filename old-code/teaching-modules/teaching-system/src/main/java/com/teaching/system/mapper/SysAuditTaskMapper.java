package com.teaching.system.mapper;

import com.teaching.system.domain.SysAuditTask;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 * 审核任务Mapper接口
 *
 * @author teaching
 * @date 2025-10-16
 */
@Mapper
public interface SysAuditTaskMapper {
    /**
     * 查询审核任务
     *
     * @param taskId 审核任务主键
     * @return 审核任务
     */
    public SysAuditTask selectSysAuditTaskByTaskId(Long taskId);

    /**
     * 查询审核任务详情 带任务审核类型
     * @param taskId
     * @return
     */
    public SysAuditTask selectSysAuditTaskAndAuditTypeByTaskId(Long taskId);

    /**
     * 查询审核任务列表
     *
     * @param sysAuditTask 审核任务
     * @return 审核任务集合
     */
    public List<SysAuditTask> selectSysAuditTaskList(SysAuditTask sysAuditTask);

    /**
     * 根据审核ids查询详情信息
     * @param audits
     * @return
     */
    public List<SysAuditTask> selectSysAuditTaskListByIds(Long[] audits);

    /**
     * 根据业务表和业务id查询详情信息  废了
     *
     * @param businessTable 业务表名称
     * @param businessId    业务id
     * @return list
     */
    public List<Map<String, Object>> selectBusinessDetail(@Param("businessTable") String businessTable, @Param("businessId") Long businessId);

    /**
     * 修改业务表审核状态 通过表名、id
     *
     * @param businessTable 表名
     * @param businessId    主键
     * @param checkStatus   状态
     * @return 结果
     */
    public int updateAuditStatusByTableAndId(@Param("businessTable") String businessTable, @Param("businessId") Long businessId, @Param("checkStatus") String checkStatus);

    /**
     * 根据登录人缓存信息查询列表进行中的审核任务信息。
     * 管理员查全部
     * @param conditions 登录人缓存的信息
     * @param isAdmin 管理员标记
     * @return list
     */
    public List<SysAuditTask> selectSysAuditTaskListByUserCache(@Param("conditions") List<Map<String, Long>> conditions
            ,@Param("isAdmin") Boolean isAdmin
            ,@Param("auditType") String auditType
            ,@Param("auditTitle") String auditTitle
            ,@Param("subPer") String subPer);

    /**
     * 查询已完成的任务列表 管理员专用
     * @param auditType
     * @param auditTitle
     * @param subPer
     * @return
     */
    public List<SysAuditTask> selectSysAuditTaskFinishedListAdmin(@Param("auditType") String auditType
            ,@Param("auditTitle") String auditTitle
            ,@Param("subPer") String subPer
    ,@Param("checkStatus") String checkStatus);

    /**
     * 查询已完成的任务列表
     * @param checkPer
     * @return
     */
    public List<SysAuditTask> selectFinishedList(@Param("checkPer") Long checkPer
            ,@Param("auditType") String auditType
            ,@Param("auditTitle") String auditTitle
            ,@Param("subPer") String subPer
    ,@Param("checkStatus") String checkStatus);

    /**
     * 新增审核任务
     *
     * @param sysAuditTask 审核任务
     * @return 结果
     */
    public int insertSysAuditTask(SysAuditTask sysAuditTask);

    /**
     * 修改审核任务
     *
     * @param sysAuditTask 审核任务
     * @return 结果
     */
    public int updateSysAuditTask(SysAuditTask sysAuditTask);

    /**
     * 删除审核任务
     *
     * @param taskId 审核任务主键
     * @return 结果
     */
    public int deleteSysAuditTaskByTaskId(Long taskId);

    /**
     * 批量删除审核任务
     *
     * @param taskIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteSysAuditTaskByTaskIds(Long[] taskIds);

    /**
     * 根据审核任务id查询审核意见
     * @param tableName
     * @param businessId
     * @return
     */
    public String selectCheckOpinionByTaskId(@Param("tableName") String tableName,@Param("businessId") Long businessId);

    /**
     * 根据审核任务id查询下一个审核人
     * @param auditId
     * @param configIds
     * @return
     */
    public String selectNextPersonName(@Param("auditId") Long auditId,@Param("configIds") List<Long> configIds);

    /**
     * 根据审核任务id查询所有节点信息
     * @param auditId
     * @return
     */
    public List<Map<String,Object>> selectAllNodeByAuditId(@Param("auditId") Long auditId);
}
