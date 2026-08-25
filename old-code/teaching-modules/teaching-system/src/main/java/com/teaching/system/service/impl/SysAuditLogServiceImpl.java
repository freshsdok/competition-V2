package com.teaching.system.service.impl;

import java.util.List;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.api.domain.SysAuditLog;
import com.teaching.system.mapper.SysAuditLogMapper;
import com.teaching.system.service.ISysAuditLogService;

/**
 * 审计日志服务层实现
 * 
 * @author teaching
 */
@Service
public class SysAuditLogServiceImpl implements ISysAuditLogService
{
    @Autowired
    private SysAuditLogMapper auditLogMapper;

    /**
     * 查询审计日志
     * 
     * @param auditId 审计日志主键
     * @return 审计日志
     */
    @Override
    public SysAuditLog selectAuditLogById(Long auditId)
    {
        return auditLogMapper.selectAuditLogById(auditId);
    }

    /**
     * 查询审计日志列表
     * 
     * @param auditLog 审计日志
     * @return 审计日志集合
     */
    @Override
    public List<SysAuditLog> selectAuditLogList(SysAuditLog auditLog)
    {
        return auditLogMapper.selectAuditLogList(auditLog);
    }

    /**
     * 新增审计日志
     * 
     * @param auditLog 审计日志
     * @return 结果
     */
    @Override
    public int insertAuditLog(SysAuditLog auditLog)
    {
        return auditLogMapper.insertAuditLog(auditLog);
    }

    /**
     * 修改审计日志（审计状态）
     * 
     * @param auditLog 审计日志
     * @return 结果
     */
    @Override
    public int updateAuditLog(SysAuditLog auditLog)
    {
        return auditLogMapper.updateAuditLog(auditLog);
    }

    /**
     * 批量删除审计日志
     * 
     * @param auditIds 需要删除的审计日志主键
     * @return 结果
     */
    @Override
    public int deleteAuditLogByIds(Long[] auditIds)
    {
        return auditLogMapper.deleteAuditLogByIds(auditIds);
    }

    /**
     * 删除审计日志信息
     * 
     * @param auditId 审计日志主键
     * @return 结果
     */
    @Override
    public int deleteAuditLogById(Long auditId)
    {
        return auditLogMapper.deleteAuditLogById(auditId);
    }

    /**
     * 清空审计日志
     */
    @Override
    public void cleanAuditLog()
    {
        auditLogMapper.cleanAuditLog();
    }

    /**
     * 统计审计日志（按审计类型）
     * 
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> countByAuditType()
    {
        return auditLogMapper.countByAuditType();
    }

    /**
     * 统计审计日志（按风险级别）
     * 
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> countByRiskLevel()
    {
        return auditLogMapper.countByRiskLevel();
    }

    /**
     * 统计异常行为
     * 
     * @return 统计结果
     */
    @Override
    public List<Map<String, Object>> countAbnormalBehavior()
    {
        return auditLogMapper.countAbnormalBehavior();
    }
}
