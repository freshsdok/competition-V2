package com.teaching.competition.service;

import com.teaching.system.api.domain.OperationConfig;

import java.util.List;

/**
 * 操作权限配置Service接口
 *
 * @author teaching
 * @date 2026-01-24
 */
public interface IOperationConfigService
{
    /**
     * 查询操作权限配置
     *
     * @param id 操作权限配置主键
     * @return 操作权限配置
     */
    public OperationConfig selectOperationConfigById(Long id);
    /**
     * 查询修改范围
     * @param competitionSeriesId
     * @param operationType
     * @return
     */
    public String getModifyScopeBySeriesIdAndOperationType(Long competitionSeriesId, String operationType);

    /**
     * 查询操作权限配置列表
     *
     * @param operationConfig 操作权限配置
     * @return 操作权限配置集合
     */
    public List<OperationConfig> selectOperationConfigList(OperationConfig operationConfig);

    /**
     * 新增操作权限配置
     *
     * @param operationConfig 操作权限配置
     * @return 结果
     */
    public int insertOperationConfig(OperationConfig operationConfig);

    public int batchInsertOperationConfig(List<OperationConfig> operationConfigList);

    /**
     * 修改操作权限配置
     *
     * @param operationConfig 操作权限配置
     * @return 结果
     */
    public int updateOperationConfig(OperationConfig operationConfig);

    /**
     * 批量删除操作权限配置
     *
     * @param ids 需要删除的操作权限配置主键集合
     * @return 结果
     */
    public int deleteOperationConfigByIds(Long[] ids);

    /**
     * 删除操作权限配置信息
     *
     * @param id 操作权限配置主键
     * @return 结果
     */
    public int deleteOperationConfigById(Long id);

    String getCompetitionFee(String secondLevelCode);
}
