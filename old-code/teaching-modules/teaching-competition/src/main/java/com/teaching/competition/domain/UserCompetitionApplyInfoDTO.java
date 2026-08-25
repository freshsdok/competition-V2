package com.teaching.competition.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.OperationConfig;

import java.util.List;
import java.util.Map;

public class UserCompetitionApplyInfoDTO extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 关联比赛code
     */
    private Long competitionSeriesId;

    /**
     * 赛事名称
     */
    private String competitionName;

    /**
     * 赛事赛道名称
     */
    private String competitionTrackName;

    /**
     * 赛事赛道id
     */
    private String competitionTrackId;

    /**
     * 二级分类编码
     */
    private String secondLevelCode;

    /**
     * 二级分类名称
     */
    private String secondLevelName;

    /**
     * 团队编号
     */
    private String teamCode;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 队员人员变更操作剩余次数
     */
    private Long memberOperateCount = 0L;
    /**
     * 指导教师人员变更操作剩余次数
     */
    private Long guideTeacherOperateCount = 0L;
    /**
     * 团队更换组别操作剩余次数
     */
    private Long secondLevelOperateCount = 0L;
    /**
     * 团队退费重缴剩余次数
     */
    private Long repaymentOperateCount = 0L;

    /**
     * 队员报名信息
     */
    private List<CompetitionApplyInfo> competitionApplyInfoList;

    /**
     * 指导教师信息
     */
    private List<CompetitionApplyInfo> guideTeacherApplyInfoList;

    /**
     * 退赛审核信息
     */
    private Map<String, Object> retiredAuditInfo;
    /**
     * 退费重缴审核信息
     */
    private Map<String, Object> repaymentAuditInfo;
    /**
     * 队员减少变更审核信息
     */
    private Map<String, Object> changeAuditInfo;

    /**
     * 是否可以发起流程
     * true 可以发起流程,false 不可以发起流程
     */
    private Boolean flag;
    /**
     * 检查有没有进行中的支付流程  0-没有，1-有
     */
    private int orderPayFlag;

    // 团队操作过状态
    private String operationStatus;

    public int getOrderPayFlag() {
        return orderPayFlag;
    }

    public void setOrderPayFlag(int orderPayFlag) {
        this.orderPayFlag = orderPayFlag;
    }

    public Boolean getFlag() {
        return flag;
    }

    public void setFlag(Boolean flag) {
        this.flag = flag;
    }

    public Map<String, Object> getChangeAuditInfo() {
        return changeAuditInfo;
    }

    public void setChangeAuditInfo(Map<String, Object> changeAuditInfo) {
        this.changeAuditInfo = changeAuditInfo;
    }

    public Map<String, Object> getRetiredAuditInfo() {
        return retiredAuditInfo;
    }

    public void setRetiredAuditInfo(Map<String, Object> retiredAuditInfo) {
        this.retiredAuditInfo = retiredAuditInfo;
    }

    public Map<String, Object> getRepaymentAuditInfo() {
        return repaymentAuditInfo;
    }

    public void setRepaymentAuditInfo(Map<String, Object> repaymentAuditInfo) {
        this.repaymentAuditInfo = repaymentAuditInfo;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getCompetitionTrackId() {
        return competitionTrackId;
    }

    public void setCompetitionTrackId(String competitionTrackId) {
        this.competitionTrackId = competitionTrackId;
    }

    public String getSecondLevelCode() {
        return secondLevelCode;
    }

    public void setSecondLevelCode(String secondLevelCode) {
        this.secondLevelCode = secondLevelCode;
    }

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public Long getMemberOperateCount() {
        return memberOperateCount;
    }

    public void setMemberOperateCount(Long memberOperateCount) {
        this.memberOperateCount = memberOperateCount;
    }

    public Long getGuideTeacherOperateCount() {
        return guideTeacherOperateCount;
    }

    public void setGuideTeacherOperateCount(Long guideTeacherOperateCount) {
        this.guideTeacherOperateCount = guideTeacherOperateCount;
    }

    public Long getSecondLevelOperateCount() {
        return secondLevelOperateCount;
    }

    public void setSecondLevelOperateCount(Long secondLevelOperateCount) {
        this.secondLevelOperateCount = secondLevelOperateCount;
    }

    public Long getRepaymentOperateCount() {
        return repaymentOperateCount;
    }

    public void setRepaymentOperateCount(Long repaymentOperateCount) {
        this.repaymentOperateCount = repaymentOperateCount;
    }

    public List<CompetitionApplyInfo> getCompetitionApplyInfoList() {
        return competitionApplyInfoList;
    }

    public void setCompetitionApplyInfoList(List<CompetitionApplyInfo> competitionApplyInfoList) {
        this.competitionApplyInfoList = competitionApplyInfoList;
    }

    public List<CompetitionApplyInfo> getGuideTeacherApplyInfoList() {
        return guideTeacherApplyInfoList;
    }

    public void setGuideTeacherApplyInfoList(List<CompetitionApplyInfo> guideTeacherApplyInfoList) {
        this.guideTeacherApplyInfoList = guideTeacherApplyInfoList;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }
}
