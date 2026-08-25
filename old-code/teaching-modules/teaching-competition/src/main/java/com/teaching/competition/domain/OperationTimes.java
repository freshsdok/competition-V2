package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 队伍操作次数对象 operation_times
 *
 * @author teaching
 * @date 2026-01-24
 */
public class OperationTimes extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     *
     */
    private Long id;

    /**
     * 队伍code
     */
    @Excel(name = "队伍code")
    private String teamCode;

    /**
     * 报名信息ID
     */
    @Excel(name = "报名信息ID")
    private Long memberId;

    /**
     * 已使用次数
     */
    @Excel(name = "已使用次数")
    private Long usedTimes;

    /**
     * 配置id
     */
    @Excel(name = "配置id")
    private Long configId;

    /**
     * 操作类型 group:更换组别，info:修改信息，change人员变更
     */
    private String operationType;

    /**
     * 最后一次操作时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    @Excel(name = "最后一次操作时间", width = 30, dateFormat = "yyyy-MM-dd")
    private Date lastOperationTime;

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setUsedTimes(Long usedTimes) {
        this.usedTimes = usedTimes;
    }

    public Long getUsedTimes() {
        return usedTimes;
    }

    public void setConfigId(Long configId) {
        this.configId = configId;
    }

    public Long getConfigId() {
        return configId;
    }

    public void setLastOperationTime(Date lastOperationTime) {
        this.lastOperationTime = lastOperationTime;
    }

    public Date getLastOperationTime() {
        return lastOperationTime;
    }

    public String getOperationType() {
        return operationType;
    }

    public void setOperationType(String operationType) {
        this.operationType = operationType;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("teamCode", getTeamCode())
                .append("memberId", getMemberId())
                .append("usedTimes", getUsedTimes())
                .append("configId", getConfigId())
                .append("lastOperationTime", getLastOperationTime())
                .append("createTime", getCreateTime())
                .append("updateTime", getUpdateTime())
                .toString();
    }
}
