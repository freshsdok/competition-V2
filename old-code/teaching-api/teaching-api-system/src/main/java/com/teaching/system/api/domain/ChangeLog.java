package com.teaching.system.api.domain;


import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.teaching.common.core.annotation.ExcelDictFormat;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 参赛信息变动日志对象 change_log
 *
 * @author teaching
 * @date 2026-01-28
 */
public class ChangeLog extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 主键
     */
    private Long id;

    /**
     * 队伍ID
     */
    private String teamId;
    /**
     * 变更时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "变动时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date changeTime;


    @Excel(name = "申请人")
    private String createBy;

    /**
     * 变更类型
     */
    @Excel(name = "数据变动类型",readConverterExp = "info=人员信息变更,change=团队人员变更,group=团队组别变更")
    private String changeType;

    /**
     * 结果
     */
    @Excel(name = "结果")
    private String result;

    /**
     * 团队名称
     */
    @Excel(name = "团队名称")
    private String teamName;

    /**
     * 报名信息记录ID
     */
    private Long memberId;

    /**
     * 操作人ID
     */
    private Long operatorUserId;


    /**
     * 变更详情描述
     */
    @Excel(name = "变动详情")
    private String changeDetails;

    /**
     * 操作IP
     */
//    @Excel(name = "操作IP")
    private String ipAddress;

    /**
     * 旧数据(JSON)
     */
    @Excel(name = "旧数据(JSON)")
    private String oldData;

    private List<Map<String,Object>> oldDataMap;

    /**
     * 新数据(JSON)
     */
    @Excel(name = "新数据(JSON)")
    private String newData;

    private List<Map<String,Object>> newDataMap;


    /**
     * 版本
     */
    private Long version;

    /**
     * 0-存在，2-删除
     */
    private String delFlag = "0";

    /**
     * 数据权限用户id
     */
    private Long userId;

    /**
     * 数据权限机构id
     */
    private Long orgId;

    /**
     * 导出类型filter:检索结果,all:全部数据
     */
    private String exportType;


    public List<Map<String, Object>> getOldDataMap() {
        return oldDataMap;
    }

    public void setOldDataMap(List<Map<String, Object>> oldDataMap) {
        this.oldDataMap = oldDataMap;
    }

    public List<Map<String, Object>> getNewDataMap() {
        return newDataMap;
    }

    public void setNewDataMap(List<Map<String, Object>> newDataMap) {
        this.newDataMap = newDataMap;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    @Override
    public String getCreateBy() {
        return createBy;
    }

    @Override
    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setTeamId(String teamId) {
        this.teamId = teamId;
    }

    public String getTeamId() {
        return teamId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public void setOperatorUserId(Long operatorUserId) {
        this.operatorUserId = operatorUserId;
    }

    public Long getOperatorUserId() {
        return operatorUserId;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeTime(Date changeTime) {
        this.changeTime = changeTime;
    }

    public Date getChangeTime() {
        return changeTime;
    }

    public void setOldData(String oldData) {
        this.oldData = oldData;
    }

    public String getOldData() {
        return oldData;
    }

    public void setNewData(String newData) {
        this.newData = newData;
    }

    public String getNewData() {
        return newData;
    }

    public void setChangeDetails(String changeDetails) {
        this.changeDetails = changeDetails;
    }

    public String getChangeDetails() {
        return changeDetails;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResult() {
        return result;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public Long getVersion() {
        return version;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public Long getOrgId() {
        return orgId;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("teamId", getTeamId())
                .append("memberId", getMemberId())
                .append("operatorUserId", getOperatorUserId())
                .append("changeType", getChangeType())
                .append("changeTime", getChangeTime())
                .append("oldData", getOldData())
                .append("newData", getNewData())
                .append("changeDetails", getChangeDetails())
                .append("result", getResult())
                .append("ipAddress", getIpAddress())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .toString();
    }
}
