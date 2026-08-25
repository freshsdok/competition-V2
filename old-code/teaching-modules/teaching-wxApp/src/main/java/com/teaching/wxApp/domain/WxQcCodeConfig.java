package com.teaching.wxApp.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 二维码配置对象 wx_qc_code_config
 *
 * @author teaching
 * @date 2026-04-08
 */
public class WxQcCodeConfig extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 配置id
     */
    private Long codeConfigId;

    /**
     * 配置名称
     */
    private String codeConfigName;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 用户组ids
     */
    private String userGroupIds;
    /**
     * 用户组名称
     */
    private String userGroupNames;

    /**
     * 成功提示语标题
     */
    private String successHintTitle;

    /**
     * 成功提示语描述
     */
    private String successHintDesc;

    /**
     * 考场规则
     */
    private String examinationHallRuler;

    /**
     * 考生承诺
     */
    private String examinationHallPromise;

    /**
     * 非正常提示标题
     */
    private String improperTitle;

    /**
     * 非正常提示语描述
     */
    private String improperDesc;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标识
     */
    private String delFlag;

    /**
     * 不是是提示信息
     */
    private String notMeMsg;
    /**
     * 二维码生成记录集合
     */
    private List<WxQcCodeRecord> wxQcCodeRecordList;


    /**
     * 赛事名称
     */
    private String competitionName;
    /**
     * 赛事系列名称
     */
    private String competitionSeriesName;

    /**
     * 赛事状态
     */
    private String checkStatus;

    /**
     * 赛事开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date competitionStartTime;
    /**
     * 赛事结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date competitionEndTime;

    /**
     * true是修改提示语标记
     */
    private Boolean msgFlag = false;

    /**
     * 用户组id列表
     */
    private List<String>groupIds;

    public List<String> getGroupIds() {
        return groupIds;
    }

    public void setGroupIds(List<String> groupIds) {
        this.groupIds = groupIds;
    }

    public Boolean getMsgFlag() {
        return msgFlag;
    }

    public void setMsgFlag(Boolean msgFlag) {
        this.msgFlag = msgFlag;
    }

    public String getUserGroupNames() {
        return userGroupNames;
    }

    public void setUserGroupNames(String userGroupNames) {
        this.userGroupNames = userGroupNames;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public Date getCompetitionStartTime() {
        return competitionStartTime;
    }

    public void setCompetitionStartTime(Date competitionStartTime) {
        this.competitionStartTime = competitionStartTime;
    }

    public Date getCompetitionEndTime() {
        return competitionEndTime;
    }

    public void setCompetitionEndTime(Date competitionEndTime) {
        this.competitionEndTime = competitionEndTime;
    }

    public String getNotMeMsg() {
        return notMeMsg;
    }

    public void setNotMeMsg(String notMeMsg) {
        this.notMeMsg = notMeMsg;
    }

    public Long getCodeConfigId() {
        return codeConfigId;
    }

    public void setCodeConfigId(Long codeConfigId) {
        this.codeConfigId = codeConfigId;
    }

    public String getCodeConfigName() {
        return codeConfigName;
    }

    public void setCodeConfigName(String codeConfigName) {
        this.codeConfigName = codeConfigName;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getUserGroupIds() {
        return userGroupIds;
    }

    public void setUserGroupIds(String userGroupIds) {
        this.userGroupIds = userGroupIds;
    }

    public String getSuccessHintTitle() {
        return successHintTitle;
    }

    public void setSuccessHintTitle(String successHintTitle) {
        this.successHintTitle = successHintTitle;
    }

    public String getSuccessHintDesc() {
        return successHintDesc;
    }

    public void setSuccessHintDesc(String successHintDesc) {
        this.successHintDesc = successHintDesc;
    }

    public String getExaminationHallRuler() {
        return examinationHallRuler;
    }

    public void setExaminationHallRuler(String examinationHallRuler) {
        this.examinationHallRuler = examinationHallRuler;
    }

    public String getExaminationHallPromise() {
        return examinationHallPromise;
    }

    public void setExaminationHallPromise(String examinationHallPromise) {
        this.examinationHallPromise = examinationHallPromise;
    }

    public String getImproperTitle() {
        return improperTitle;
    }

    public void setImproperTitle(String improperTitle) {
        this.improperTitle = improperTitle;
    }

    public String getImproperDesc() {
        return improperDesc;
    }

    public void setImproperDesc(String improperDesc) {
        this.improperDesc = improperDesc;
    }

    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public List<WxQcCodeRecord> getWxQcCodeRecordList() {
        return wxQcCodeRecordList;
    }

    public void setWxQcCodeRecordList(List<WxQcCodeRecord> wxQcCodeRecordList) {
        this.wxQcCodeRecordList = wxQcCodeRecordList;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("codeConfigId", getCodeConfigId())
                .append("codeConfigName", getCodeConfigName())
                .append("competitionSeriesId", getCompetitionSeriesId())
                .append("userGroupIds", getUserGroupIds())
                .append("successHintTitle", getSuccessHintTitle())
                .append("successHintDesc", getSuccessHintDesc())
                .append("examinationHallRuler", getExaminationHallRuler())
                .append("examinationHallPromise", getExaminationHallPromise())
                .append("improperTitle", getImproperTitle())
                .append("improperDesc", getImproperDesc())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("remark", getRemark())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("wxQcCodeRecordList", getWxQcCodeRecordList())
                .toString();
    }
}
