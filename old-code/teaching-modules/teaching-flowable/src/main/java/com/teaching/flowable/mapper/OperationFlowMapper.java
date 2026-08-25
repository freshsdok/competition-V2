package com.teaching.flowable.mapper;


import com.teaching.system.api.domain.OperationFlow;
import org.apache.ibatis.annotations.Param;

import java.util.List;

/**
 * 团队信息操作和流程关联Mapper接口
 *
 * @author teaching
 * @date 2026-02-02
 */
public interface OperationFlowMapper {
    /**
     * 查询团队信息操作和流程关联
     *
     * @param teamCode 团队信息操作和流程关联主键
     * @return 团队信息操作和流程关联
     */
    public List<OperationFlow> selectOperationFlowByTeamCode(String teamCode);

    public OperationFlow getTeamCodeOperatorType(String teamCode);


    /**
     * 查询团队信息操作和流程关联列表
     *
     * @param operationFlow 团队信息操作和流程关联
     * @return 团队信息操作和流程关联集合
     */
    public List<OperationFlow> selectOperationFlowList(OperationFlow operationFlow);

    /**
     * 查询团队信息操作和流程关联列表
     * 同一个团队每个类型的最新的
     * @param teamCode 团队编码
     * @return
     */
    public List<OperationFlow> selectOperationNewFlow(String teamCode);

    /**
     * 新增团队信息操作和流程关联
     *
     * @param operationFlow 团队信息操作和流程关联
     * @return 结果
     */
    public int insertOperationFlow(OperationFlow operationFlow);

    /**
     * 修改团队信息操作和流程关联
     *
     * @param operationFlow 团队信息操作和流程关联
     * @return 结果
     */
    public int updateStatusByFlowId(OperationFlow operationFlow);

    /**
     * 删除团队信息操作和流程关联
     *
     * @param teamCode 团队信息操作和流程关联主键
     * @return 结果
     */
    public int deleteOperationFlowByTeamCode(String teamCode);

    /**
     * 批量删除团队信息操作和流程关联
     *
     * @param teamCodes 需要删除的数据主键集合
     * @return 结果
     */
    public int deleteOperationFlowByTeamCodes(String[] teamCodes);
}
