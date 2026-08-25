package com.teaching.competition.mapper;

import com.teaching.competition.domain.OperationTimes;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 队伍操作次数Mapper接口
 *
 * @author teaching
 * @date 2026-01-24
 */
public interface OperationTimesMapper {
    /**
     * 查询队伍操作次数
     *
     * @param id 队伍操作次数主键
     * @return 队伍操作次数
     */
    public OperationTimes selectOperationTimesById(Long id);

    /**
     * 根据配置id查询队伍操作次数
     * @param configId
     * @return
     */
    public List<OperationTimes> selectOperationTimesByConfigId(@Param("configId") Long configId, @Param("teamCode") String teamCode);

    /**
     * 根据队伍编号查询操作次数
     * @param teamCode
     * @return
     */
    public List<OperationTimes> selectOperationTimesByTeamCode(String teamCode);

    public OperationTimes selectOperationTimesByTeamCodeAndOperationType(String teamCode,String operationType);

    /**
     * 查询队伍操作次数列表
     *
     * @param operationTimes 队伍操作次数
     * @return 队伍操作次数集合
     */
    public List<OperationTimes> selectOperationTimesList(OperationTimes operationTimes);

    /**
     * 新增队伍操作次数
     *
     * @param operationTimes 队伍操作次数
     * @return 结果
     */
    public int insertOperationTimes(OperationTimes operationTimes);

    /**
     * 修改队伍操作次数
     *
     * @param operationTimes 队伍操作次数
     * @return 结果
     */
    public int updateOperationTimes(OperationTimes operationTimes);

    /**
     * 删除队伍操作次数
     *
     * @param id 队伍操作次数主键
     * @return 结果
     */
    public int deleteOperationTimesById(Long id);

    /**
     * 批量删除队伍操作次数
     *
     * @param ids 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOperationTimesByIds(Long[] ids);
}
