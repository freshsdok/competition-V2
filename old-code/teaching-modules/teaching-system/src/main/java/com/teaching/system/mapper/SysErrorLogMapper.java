package com.teaching.system.mapper;

import java.util.List;
import org.apache.ibatis.annotations.Mapper;
import com.teaching.system.api.domain.SysErrorLog;

/**
 * 错误日志Mapper接口
 * 
 * @author teaching
 */
@Mapper
public interface SysErrorLogMapper
{
    /**
     * 查询错误日志
     * 
     * @param errorId 错误日志主键
     * @return 错误日志
     */
    public SysErrorLog selectErrorLogById(Long errorId);

    /**
     * 查询错误日志列表
     * 
     * @param errorLog 错误日志
     * @return 错误日志集合
     */
    public List<SysErrorLog> selectErrorLogList(SysErrorLog errorLog);

    /**
     * 新增错误日志
     * 
     * @param errorLog 错误日志
     * @return 结果
     */
    public int insertErrorLog(SysErrorLog errorLog);

    /**
     * 修改错误日志（处理状态）
     * 
     * @param errorLog 错误日志
     * @return 结果
     */
    public int updateErrorLog(SysErrorLog errorLog);

    /**
     * 删除错误日志
     * 
     * @param errorId 错误日志主键
     * @return 结果
     */
    public int deleteErrorLogById(Long errorId);

    /**
     * 批量删除错误日志
     * 
     * @param errorIds 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteErrorLogByIds(Long[] errorIds);

    /**
     * 清空错误日志
     */
    public void cleanErrorLog();
}
