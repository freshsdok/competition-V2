package com.teaching.system.service.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import com.teaching.system.api.domain.SysErrorLog;
import com.teaching.system.mapper.SysErrorLogMapper;
import com.teaching.system.service.ISysErrorLogService;

/**
 * 错误日志服务层实现
 * 
 * @author teaching
 */
@Service
public class SysErrorLogServiceImpl implements ISysErrorLogService
{
    @Autowired
    private SysErrorLogMapper errorLogMapper;

    /**
     * 查询错误日志
     * 
     * @param errorId 错误日志主键
     * @return 错误日志
     */
    @Override
    public SysErrorLog selectErrorLogById(Long errorId)
    {
        return errorLogMapper.selectErrorLogById(errorId);
    }

    /**
     * 查询错误日志列表
     * 
     * @param errorLog 错误日志
     * @return 错误日志集合
     */
    @Override
    public List<SysErrorLog> selectErrorLogList(SysErrorLog errorLog)
    {
        return errorLogMapper.selectErrorLogList(errorLog);
    }

    /**
     * 新增错误日志
     * 
     * @param errorLog 错误日志
     * @return 结果
     */
    @Override
    public int insertErrorLog(SysErrorLog errorLog)
    {
        return errorLogMapper.insertErrorLog(errorLog);
    }

    /**
     * 修改错误日志（处理状态）
     * 
     * @param errorLog 错误日志
     * @return 结果
     */
    @Override
    public int updateErrorLog(SysErrorLog errorLog)
    {
        return errorLogMapper.updateErrorLog(errorLog);
    }

    /**
     * 批量删除错误日志
     * 
     * @param errorIds 需要删除的错误日志主键
     * @return 结果
     */
    @Override
    public int deleteErrorLogByIds(Long[] errorIds)
    {
        return errorLogMapper.deleteErrorLogByIds(errorIds);
    }

    /**
     * 删除错误日志信息
     * 
     * @param errorId 错误日志主键
     * @return 结果
     */
    @Override
    public int deleteErrorLogById(Long errorId)
    {
        return errorLogMapper.deleteErrorLogById(errorId);
    }

    /**
     * 清空错误日志
     */
    @Override
    public void cleanErrorLog()
    {
        errorLogMapper.cleanErrorLog();
    }
}
