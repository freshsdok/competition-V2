package com.teaching.competition.domain;

import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.system.api.domain.CompetitionApplyInfo;
import com.teaching.system.api.domain.OperationConfig;

import java.util.List;

public class UserCompetitionApplyInfoTeam extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 关联比赛code */
    private Long competitionSeriesId;

    /** 赛事名称 */
    private String competitionName;

    /** 赛事赛道名称 */
    private String competitionTrackName;

    /** 赛事赛道id */
    private String competitionTrackId;

    /** 二级分类编码 */
    private String secondLevelCode;

    /** 二级分类名称 */
    private String secondLevelName;

    /** 团队编号 */
    private String teamCode;

    /** 团队名称 */
    private String teamName;

    /** 变更类型 */
    private String changeType;

    private String registrationTime;

    // 团队操作过状态
    private String operationStatus;

    // 标识判断是修改得队员还是指导教师
    private String competitionRoleName;

    // 0 是单行操作校验 1或者不穿任何值是总行操作校验
    private String oneLineFlag;

    /** 报名信息 */
    private List<CompetitionApplyInfo> competitionApplyInfoList;

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

    public List<CompetitionApplyInfo> getCompetitionApplyInfoList() {
        return competitionApplyInfoList;
    }

    public void setCompetitionApplyInfoList(List<CompetitionApplyInfo> competitionApplyInfoList) {
        this.competitionApplyInfoList = competitionApplyInfoList;
    }

    public String getChangeType() {
        return changeType;
    }

    public void setChangeType(String changeType) {
        this.changeType = changeType;
    }

    public String getRegistrationTime() {
        return registrationTime;
    }

    public void setRegistrationTime(String registrationTime) {
        this.registrationTime = registrationTime;
    }

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public String getOperationStatus() {
        return operationStatus;
    }

    public void setOperationStatus(String operationStatus) {
        this.operationStatus = operationStatus;
    }

    public String getOneLineFlag() {
        return oneLineFlag;
    }

    public void setOneLineFlag(String oneLineFlag) {
        this.oneLineFlag = oneLineFlag;
    }
}
