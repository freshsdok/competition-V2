package com.teaching.system.api.domain;


import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

/**
 * 团队信息操作和流程关联对象 operation_flow
 *
 * @author teaching
 * @date 2026-02-02
 */
public class OperationFlow extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 团队编号
     */
    private String teamCode;

    /**
     * 流程唯一标识
     */
    private String flowId;

    /**
     * 流程类型
     */
    private String flowType;
    /**
     * 流程状态 running进行中 completed 完成
     */
    private String flowStatus;


    public OperationFlow() {
    }

    public OperationFlow(String teamCode, String flowId, String flowType, String flowStatus) {
        this.teamCode = teamCode;
        this.flowId = flowId;
        this.flowType = flowType;
        this.flowStatus = flowStatus;
    }

    public OperationFlow(String flowId, String flowStatus) {
        this.flowId = flowId;
        this.flowStatus = flowStatus;
    }

    public OperationFlow(String teamCode, String flowType, String flowStatus) {
        this.teamCode = teamCode;
        this.flowType = flowType;
        this.flowStatus = flowStatus;
    }

    public String getFlowStatus() {
        return flowStatus;
    }

    public void setFlowStatus(String flowStatus) {
        this.flowStatus = flowStatus;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setFlowId(String flowId) {
        this.flowId = flowId;
    }

    public String getFlowId() {
        return flowId;
    }

    public void setFlowType(String flowType) {
        this.flowType = flowType;
    }

    public String getFlowType() {
        return flowType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("teamCode", getTeamCode())
                .append("flowId", getFlowId())
                .append("flowType", getFlowType())
                .toString();
    }
}
