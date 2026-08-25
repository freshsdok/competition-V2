package com.teaching.competition.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;
import java.util.List;

/**
 * 获奖公示明细对象 award_details
 *
 * @author teaching
 * @date 2026-05-12
 */
public class AwardDetails extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * $column.columnComment
     */
    private Long id;

    /**
     * 主表id
     */
    private Long awardPublicityId;

    /**
     * 团队编号
     */
    @Excel(name = "团队编号",required = true)
    private String teamCode;

    /**
     * 学校名称
     */
    private String schoolName;

    /**
     * 团队名称
     */
    private String teamName;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 赛道code
     */
    private String competitionTrackId;

    /**
     * 组别code
     */
    private String secondLevelCode;

    /**
     * 赛事名称
     */
    private String competitionName;

    /**
     * 赛道名称
     */
    private String competitionTrackName;

    /**
     * 组别名称
     */
    private String secondLevelName;

    /**
     * 学校id
     */
    private String schoolId;

    /**
     * 奖项名称
     */
    @Excel(name = "奖项名称",required = true)
    private String awardsName;

    /**
     * 参赛学生姓名查询条件
     */
    private String userName;

    private String competitionRoleName;
    /**
     * 指导教师姓名查询条件
     */
    private String guiderTeacherName;

    /**
     * 参赛学生姓名集合
     */
    private List<AwardPlayerInfo> playerList;

    /**
     * 指导教师姓名集合
     */
    private List<AwardPlayerInfo> guiderTeacherList;

    /**
     * 导出类型
     */
    private String exportType;

    private Long userId;

    /**
     * 删除标识
     */
    private String delFlag;
    /**
     * 导入类型 addition追加/replace替换
     */
    private String importType;

    /** 支付时间 */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
//    @Excel(name = "支付时间", dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date payTime;

    public String getImportType() {
        return importType;
    }

    public void setImportType(String importType) {
        this.importType = importType;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public void setAwardPublicityId(Long awardPublicityId) {
        this.awardPublicityId = awardPublicityId;
    }

    public Long getAwardPublicityId() {
        return awardPublicityId;
    }

    public void setTeamCode(String teamCode) {
        this.teamCode = teamCode;
    }

    public String getTeamCode() {
        return teamCode;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public String getTeamName() {
        return teamName;
    }

    public void setAwardsName(String awardsName) {
        this.awardsName = awardsName;
    }

    public String getAwardsName() {
        return awardsName;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
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

    public String getSecondLevelName() {
        return secondLevelName;
    }

    public void setSecondLevelName(String secondLevelName) {
        this.secondLevelName = secondLevelName;
    }

    public String getSchoolName() {
        return schoolName;
    }

    public void setSchoolName(String schoolName) {
        this.schoolName = schoolName;
    }

    public String getSchoolId() {
        return schoolId;
    }

    public void setSchoolId(String schoolId) {
        this.schoolId = schoolId;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getGuiderTeacherName() {
        return guiderTeacherName;
    }

    public void setGuiderTeacherName(String guiderTeacherName) {
        this.guiderTeacherName = guiderTeacherName;
    }

    public List<AwardPlayerInfo> getPlayerList() {
        return playerList;
    }

    public void setPlayerList(List<AwardPlayerInfo> playerList) {
        this.playerList = playerList;
    }

    public List<AwardPlayerInfo> getGuiderTeacherList() {
        return guiderTeacherList;
    }

    public void setGuiderTeacherList(List<AwardPlayerInfo> guiderTeacherList) {
        this.guiderTeacherList = guiderTeacherList;
    }

    public String getCompetitionRoleName() {
        return competitionRoleName;
    }

    public void setCompetitionRoleName(String competitionRoleName) {
        this.competitionRoleName = competitionRoleName;
    }

    public String getExportType() {
        return exportType;
    }

    public void setExportType(String exportType) {
        this.exportType = exportType;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Date getPayTime() {
        return payTime;
    }

    public void setPayTime(Date payTime) {
        this.payTime = payTime;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this, ToStringStyle.MULTI_LINE_STYLE)
                .append("id", getId())
                .append("awardPublicityId", getAwardPublicityId())
                .append("teamCode", getTeamCode())
                .append("teamName", getTeamName())
                .append("awardsName", getAwardsName())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("delFlag", getDelFlag())
                .toString();
    }
}
