package com.teaching.system.service;

import java.util.List;
import com.teaching.system.domain.SysSenderMessageLog;

/**
 * 推送信息日志Service接口
 * 
 * @author teaching
 * @date 2026-01-30
 */
public interface ISysSenderMessageLogService 
{
    /**
     * 查询推送信息日志
     * 
     * @param sendId 推送信息日志主键
     * @return 推送信息日志
     */
    public SysSenderMessageLog selectSysSenderMessageLogBySendId(Long sendId);

    /**
     * 查询推送信息日志列表
     * 
     * @param sysSenderMessageLog 推送信息日志
     * @return 推送信息日志集合
     */
    public List<SysSenderMessageLog> selectSysSenderMessageLogList(SysSenderMessageLog sysSenderMessageLog);

    /**
     * 新增推送信息日志
     * 
     * @param sysSenderMessageLog 推送信息日志
     * @return 结果
     */
    public int insertSysSenderMessageLog(SysSenderMessageLog sysSenderMessageLog);

    /**
     * 修改推送信息日志
     * 
     * @param sysSenderMessageLog 推送信息日志
     * @return 结果
     */
    public int updateSysSenderMessageLog(SysSenderMessageLog sysSenderMessageLog);

    /**
     * 批量删除推送信息日志
     * 
     * @param sendIds 需要删除的推送信息日志主键集合
     * @return 结果
     */
    public int deleteSysSenderMessageLogBySendIds(Long[] sendIds);

    /**
     * 删除推送信息日志信息
     * 
     * @param sendId 推送信息日志主键
     * @return 结果
     */
    public int deleteSysSenderMessageLogBySendId(Long sendId);
}
