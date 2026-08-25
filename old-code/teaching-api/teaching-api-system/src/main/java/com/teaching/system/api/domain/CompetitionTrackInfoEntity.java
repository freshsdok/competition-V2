package com.teaching.system.api.domain;

import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

public class CompetitionTrackInfoEntity extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /** 赛道id */
    private String competitionTrackId;

    /** 赛道配置id */
    private Long competitionTrackConfigId;

    /** 赛事id */
    private Long competitionSeriesId;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 赛道设置类型 */
    private String competitionTrackType;

    /** 赛道设置类型描述 */
    private String competitionTrackTypeDesc;

    /** 二级编码 */
    private String secondLevelCode;

    /** 二级编码名称 */
    private String secondLevelName;

    /** 报名费用 */
    private String fee;

    /** 赛道描述 */
    private String competitionTrackDesc;

    /** 赛事配置审核状态 */
    private String checkStatus;

    /** 删除标识 */
    private String delFlag = "0";

    /**
     * 校验包id
     */
    private Long checkPackageId;

    public Long getCheckPackageId() {
        return checkPackageId;
    }

    public void setCheckPackageId(Long checkPackageId) {
        this.checkPackageId = checkPackageId;
    }

    public String getCompetitionTrackId() {
        return competitionTrackId;
    }

    public void setCompetitionTrackId(String competitionTrackId) {
        this.competitionTrackId = competitionTrackId;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getCompetitionTrackType() {
        return competitionTrackType;
    }

    public void setCompetitionTrackType(String competitionTrackType) {
        this.competitionTrackType = competitionTrackType;
    }

    public String getCompetitionTrackTypeDesc() {
        return competitionTrackTypeDesc;
    }

    public void setCompetitionTrackTypeDesc(String competitionTrackTypeDesc) {
        this.competitionTrackTypeDesc = competitionTrackTypeDesc;
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

    public String getFee() {
        return fee;
    }

    public void setFee(String fee) {
        this.fee = fee;
    }

    public String getCompetitionTrackDesc() {
        return competitionTrackDesc;
    }

    public void setCompetitionTrackDesc(String competitionTrackDesc) {
        this.competitionTrackDesc = competitionTrackDesc;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getDelFlag() {
        return delFlag;
    }

    public void setDelFlag(String delFlag) {
        this.delFlag = delFlag;
    }

    public Long getCompetitionTrackConfigId() {
        return competitionTrackConfigId;
    }

    public void setCompetitionTrackConfigId(Long competitionTrackConfigId) {
        this.competitionTrackConfigId = competitionTrackConfigId;
    }
}
