package com.teaching.flowable.service.impl;


import com.teaching.common.core.utils.DateUtils;
import com.teaching.flowable.mapper.OperationFlowMapper;
import com.teaching.flowable.service.IOperationFlowService;
import com.teaching.flowable.service.IWfTaskService;
import com.teaching.system.api.domain.OperationFlow;
import org.apache.commons.collections4.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 团队信息操作和流程关联Service业务层处理
 *
 * @author teaching
 * @date 2026-02-02
 */
@Service
public class OperationFlowServiceImpl implements IOperationFlowService {
    @Autowired
    private OperationFlowMapper operationFlowMapper;
    @Autowired
    private IWfTaskService flowTaskService;

    /**
     * 查询团队信息操作和流程关联
     *
     * @param teamCode 团队信息操作和流程关联主键
     * @return 团队信息操作和流程关联
     */
    @Override
    public List<OperationFlow> selectOperationFlowByTeamCode(String teamCode) {
        return operationFlowMapper.selectOperationFlowByTeamCode(teamCode);
    }

    //取团队最新操作
    @Override
    public OperationFlow getTeamCodeOperatorType(String teamCode) {
        return operationFlowMapper.getTeamCodeOperatorType(teamCode);
    }


    /**
     * 查询团队信息操作和流程关联列表
     *
     * @param operationFlow 团队信息操作和流程关联
     * @return 团队信息操作和流程关联
     */
    @Override
    public List<OperationFlow> selectOperationFlowList(OperationFlow operationFlow) {
        return operationFlowMapper.selectOperationFlowList(operationFlow);
    }

    /**
     * 根据teamCode和flowType获取流程信息
     * @param teamCode
     * @return
     */
    @Override
    public Map<String, Object> getFlowVariables(String teamCode) {
        Map<String, Object> variables = new HashMap<String, Object>();
        List<OperationFlow> newFlow = operationFlowMapper.selectOperationNewFlow(teamCode);
        if(CollectionUtils.isNotEmpty(newFlow)){
            newFlow.forEach(flow->{
                Map<String, Object> processVariablesByTraceabilityCode = flowTaskService.getProcessVariablesByTraceabilityCode(flow.getFlowId());
                processVariablesByTraceabilityCode.put("createTime", DateUtils.dateTimeFormatr(flow.getCreateTime(),"yyyy-MM-dd HH:mm:ss"));
                variables.put(flow.getFlowType(), processVariablesByTraceabilityCode);
            });
        }
        return variables;
    }

    /**
     * 新增团队信息操作和流程关联
     *
     * @param operationFlow 团队信息操作和流程关联
     * @return 结果
     */
    @Override
    public int insertOperationFlow(OperationFlow operationFlow) {
        return operationFlowMapper.insertOperationFlow(operationFlow);
    }

    /**
     * 修改团队信息操作和流程关联
     *
     * @param operationFlow 团队信息操作和流程关联
     * @return 结果
     */
    @Override
    public int updateStatusByFlowId(OperationFlow operationFlow) {
        return operationFlowMapper.updateStatusByFlowId(operationFlow);
    }

    /**
     * 批量删除团队信息操作和流程关联
     *
     * @param teamCodes 需要删除的团队信息操作和流程关联主键
     * @return 结果
     */
    @Override
    public int deleteOperationFlowByTeamCodes(String[] teamCodes) {
        return operationFlowMapper.deleteOperationFlowByTeamCodes(teamCodes);
    }

    /**
     * 删除团队信息操作和流程关联信息
     *
     * @param teamCode 团队信息操作和流程关联主键
     * @return 结果
     */
    @Override
    public int deleteOperationFlowByTeamCode(String teamCode) {
        return operationFlowMapper.deleteOperationFlowByTeamCode(teamCode);
    }
}
