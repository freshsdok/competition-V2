package com.teaching.competition.mapper;

import com.teaching.system.api.domain.OperationConfig;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 操作权限配置Mapper接口
 *
 * @author teaching
 * @date 2026-01-24
 */
public interface OperationConfigMapper
{
    /**
     * 查询操作权限配置
     *
     * @param id 操作权限配置主键
     * @return 操作权限配置
     */
    public OperationConfig selectOperationConfigById(Long id);

    /**
     * 查询操作权限配置列表
     *
     * @param operationConfig 操作权限配置
     * @return 操作权限配置集合
     */
    public List<OperationConfig> selectOperationConfigList(OperationConfig operationConfig);

    public List<OperationConfig> selectOperationConfigByCompetitionSeriesId(Long competitionSeriesId);

    /**
     * 查询修改范围
     * @param competitionSeriesId
     * @param operationType
     * @return
     */
    public String selectModifyScopeBySeriesIdAndOperationType(@Param("competitionSeriesId") Long competitionSeriesId, @Param("operationType") String operationType);

    /**
     * 新增操作权限配置
     *
     * @param operationConfig 操作权限配置
     * @return 结果
     */
    public int insertOperationConfig(OperationConfig operationConfig);

    /**
     * 批量新增操作权限配置
     *
     * @param operationConfigList 操作权限配置列表
     * @return 结果
     */
    public int batchInsertOperationConfig(List<OperationConfig> operationConfigList);

    /**
     * 修改操作权限配置
     *
     * @param operationConfig 操作权限配置
     * @return 结果
     */
    public int updateOperationConfig(OperationConfig operationConfig);

    /**
     * 删除操作权限配置
     *
     * @param id 操作权限配置主键
     * @return 结果
     */
    public int deleteOperationConfigById(Long id);

    /**
     * 批量删除操作权限配置
     *
     * @param competitionSeriesId 批量删除操作权限配置
     * @return 批量删除操作权限配置
     */
    public int deleteOperationConfigByCompetitionSeriesId(Long competitionSeriesId);

    /**
     * 批量删除操作权限配置
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOperationConfigByIds(Long[] ids);

    String getCompetitionFee(String secondLevelCode);
}
