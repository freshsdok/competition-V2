package com.teaching.system.mapper;

import java.util.List;
import java.util.Map;
import org.apache.ibatis.annotations.Mapper;
import com.teaching.system.api.domain.SysAuditLog;

/**
 * 审计日志Mapper接口
 * 
 * @author teaching
 */
@Mapper
public interface SysAuditLogMapper
{
    /**
     * 查询审计日志
     * 
     * @param auditId 审计日志主键
     * @return 审计日志
     */
    public SysAuditLog selectAuditLogById(Long auditId);

    /**
     * 查询审计日志列表
     * 
     * @param auditLog 审计日志
     * @return 审计日志集合
     */
    public List<SysAuditLog> selectAuditLogList(SysAuditLog auditLog);

    /**
     * 新增审计日志
     * 
     * @param auditLog 审计日志
     * @return 结果
     */
    public int insertAuditLog(SysAuditLog auditLog);

    /**
     * 修改审计日志（审计状态）
     * 
     * @param auditLog 审计日志
     * @return 结果
     */
    public int updateAuditLog(SysAuditLog auditLog);

    /**
     * 删除审计日志
     * 
     * @param auditId 审计日志主键
     * @return 结果
     */
    public int deleteAuditLogById(Long auditId);

    /**
     * 批量删除审计日志
     * 
     * @param auditIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteAuditLogByIds(Long[] auditIds);

    /**
     * 清空审计日志
     */
    public void cleanAuditLog();

    /**
     * 统计审计日志（按审计类型）
     * 
     * @return 统计结果
     */
    public List<Map<String, Object>> countByAuditType();

    /**
     * 统计审计日志（按风险级别）
     * 
     * @return 统计结果
     */
    public List<Map<String, Object>> countByRiskLevel();

    /**
     * 统计异常行为
     * 
     * @return 统计结果
     */
    public List<Map<String, Object>> countAbnormalBehavior();
}
