package com.teaching.flowable.service;


import com.teaching.system.api.domain.OperationFlow;

import java.util.List;
import java.util.Map;

/**
 * 团队信息操作和流程关联Service接口
 *
 * @author teaching
 * @date 2026-02-02
 */
public interface IOperationFlowService {
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
     * 根据流程id获取流程变量
     * @param teamCode
     * @return
     */
    public Map<String,Object> getFlowVariables(String teamCode);

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
     * 批量删除团队信息操作和流程关联
     *
     * @param teamCodes 需要删除的团队信息操作和流程关联主键集合
     * @return 结果
     */
    public int deleteOperationFlowByTeamCodes(String[] teamCodes);

    /**
     * 删除团队信息操作和流程关联信息
     *
     * @param teamCode 团队信息操作和流程关联主键
     * @return 结果
     */
    public int deleteOperationFlowByTeamCode(String teamCode);
}
