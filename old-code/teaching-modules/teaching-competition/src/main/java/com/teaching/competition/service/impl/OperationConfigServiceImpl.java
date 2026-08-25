package com.teaching.competition.service.impl;

import java.util.List;

import cn.hutool.core.util.NumberUtil;
import com.teaching.common.core.exception.GlobalException;
import com.teaching.common.core.utils.DateUtils;
import com.teaching.common.core.utils.StringUtils;
import com.teaching.competition.contant.ApplyConstants;
import com.teaching.competition.mapper.OperationConfigMapper;
import com.teaching.competition.service.IOperationConfigService;
import com.teaching.system.api.domain.OperationConfig;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 操作权限配置Service业务层处理
 *
 * @author teaching
 * @date 2026-01-24
 */
@Service
public class OperationConfigServiceImpl implements IOperationConfigService {
    @Autowired
    private OperationConfigMapper operationConfigMapper;

    /**
     * 查询操作权限配置
     *
     * @param id 操作权限配置主键
     * @return 操作权限配置
     */
    @Override
    public OperationConfig selectOperationConfigById(Long id) {
        return operationConfigMapper.selectOperationConfigById(id);
    }

    /**
     * 查询修改范围
     * @param competitionSeriesId
     * @param operationType
     * @return
     */
    @Override
    public String getModifyScopeBySeriesIdAndOperationType(Long competitionSeriesId, String operationType) {
        return operationConfigMapper.selectModifyScopeBySeriesIdAndOperationType(competitionSeriesId, operationType);
    }

    /**
     * 查询操作权限配置列表
     *
     * @param operationConfig 操作权限配置
     * @return 操作权限配置
     */
    @Override
    public List<OperationConfig> selectOperationConfigList(OperationConfig operationConfig) {
        return operationConfigMapper.selectOperationConfigList(operationConfig);
    }

    /**
     * 新增操作权限配置
     *
     * @param operationConfig 操作权限配置
     * @return 结果
     */
    @Override
    public int insertOperationConfig(OperationConfig operationConfig) {
        operationConfig.setCreateTime(DateUtils.getNowDate());
        return operationConfigMapper.insertOperationConfig(operationConfig);
    }

    @Override
    public int batchInsertOperationConfig(List<OperationConfig> operationConfigList) {
        if (CollectionUtils.isNotEmpty(operationConfigList)) {
            operationConfigList.stream().forEach(operationConfig -> operationConfig.setCreateTime(DateUtils.getNowDate()));
        }
        return operationConfigMapper.batchInsertOperationConfig(operationConfigList);
    }

    /**
     * 修改操作权限配置
     *
     * @param operationConfig 操作权限配置
     * @return 结果
     */
    @Override
    public int updateOperationConfig(OperationConfig operationConfig) {
        operationConfig.setUpdateTime(DateUtils.getNowDate());
        String modifyScope = operationConfig.getModifyScope();
        if (StringUtils.isNotBlank(modifyScope) && !NumberUtil.isNumber(modifyScope)) {
            throw new GlobalException("请选择正确的范围！");
        }
        return operationConfigMapper.updateOperationConfig(operationConfig);
    }

    /**
     * 批量删除操作权限配置
     *
     * @param ids 需要删除的操作权限配置主键
     * @return 结果
     */
    @Override
    public int deleteOperationConfigByIds(Long[] ids) {
        return operationConfigMapper.deleteOperationConfigByIds(ids);
    }

    /**
     * 删除操作权限配置信息
     *
     * @param id 操作权限配置主键
     * @return 结果
     */
    @Override
    public int deleteOperationConfigById(Long id) {
        return operationConfigMapper.deleteOperationConfigById(id);
    }

    @Override
    public String getCompetitionFee(String secondLevelCode) {
        return operationConfigMapper.getCompetitionFee(secondLevelCode);
    }
}
