package com.teaching.system.service.impl;

import java.util.List;
import com.teaching.common.core.utils.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.mapper.SysSenderMessageLogMapper;
import com.teaching.system.domain.SysSenderMessageLog;
import com.teaching.system.service.ISysSenderMessageLogService;

/**
 * 推送信息日志Service业务层处理
 * 
 * @author teaching
 * @date 2026-01-30
 */
@Service
public class SysSenderMessageLogServiceImpl implements ISysSenderMessageLogService 
{
    @Autowired
    private SysSenderMessageLogMapper sysSenderMessageLogMapper;

    /**
     * 查询推送信息日志
     * 
     * @param sendId 推送信息日志主键
     * @return 推送信息日志
     */
    @Override
    public SysSenderMessageLog selectSysSenderMessageLogBySendId(Long sendId)
    {
        return sysSenderMessageLogMapper.selectSysSenderMessageLogBySendId(sendId);
    }

    /**
     * 查询推送信息日志列表
     * 
     * @param sysSenderMessageLog 推送信息日志
     * @return 推送信息日志
     */
    @Override
    public List<SysSenderMessageLog> selectSysSenderMessageLogList(SysSenderMessageLog sysSenderMessageLog)
    {
        return sysSenderMessageLogMapper.selectSysSenderMessageLogList(sysSenderMessageLog);
    }

    /**
     * 新增推送信息日志
     * 
     * @param sysSenderMessageLog 推送信息日志
     * @return 结果
     */
    @Override
    public int insertSysSenderMessageLog(SysSenderMessageLog sysSenderMessageLog)
    {
        sysSenderMessageLog.setCreateTime(DateUtils.getNowDate());
        return sysSenderMessageLogMapper.insertSysSenderMessageLog(sysSenderMessageLog);
    }

    /**
     * 修改推送信息日志
     * 
     * @param sysSenderMessageLog 推送信息日志
     * @return 结果
     */
    @Override
    public int updateSysSenderMessageLog(SysSenderMessageLog sysSenderMessageLog)
    {
        sysSenderMessageLog.setUpdateTime(DateUtils.getNowDate());
        return sysSenderMessageLogMapper.updateSysSenderMessageLog(sysSenderMessageLog);
    }

    /**
     * 批量删除推送信息日志
     * 
     * @param sendIds 需要删除的推送信息日志主键
     * @return 结果
     */
    @Override
    public int deleteSysSenderMessageLogBySendIds(Long[] sendIds)
    {
        return sysSenderMessageLogMapper.deleteSysSenderMessageLogBySendIds(sendIds);
    }

    /**
     * 删除推送信息日志信息
     * 
     * @param sendId 推送信息日志主键
     * @return 结果
     */
    @Override
    public int deleteSysSenderMessageLogBySendId(Long sendId)
    {
        return sysSenderMessageLogMapper.deleteSysSenderMessageLogBySendId(sendId);
    }
}
