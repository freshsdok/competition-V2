package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;
import java.util.List;

public class CompetitionDetailInfo extends BaseEntity {

    private static final long serialVersionUID = 1L;

    /**
     * 赛事id
     */
    private Long competitionId;

    /**
     * 赛事编码
     */
    private String competitionCode;

    /**
     * 赛事名称
     */
    private String competitionName;

    /**
     * 主办方信息
     */
    private String organizer;

    /**
     * 赛事系列id
     */
    private Long competitionSeriesId;

    /**
     * 赛事系列名称
     */
    private String competitionSeriesName;

    /**
     * 赛事类型
     */
    private String competitionType;

    /**
     * 赛事类型中文
     */
    private String competitionTypeCn;

    /**
     * 赛事描述
     */
    private String competitionDesc;

    /**
     * 赛事状态
     */
    private String checkStatus;

    /**
     * 赛事开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionStartTime;

    /**
     * 赛事结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date competitionEndTime;

    /**
     * 最大参赛人数
     */
    private String competitionNumber;

    /**
     * 赛事图片
     */
    private String competitionImageName;

    /**
     * 赛事图片
     */
    private String competitionImage;

    /**
     * 赛事企业id
     */
    private String enterpriseId;

    /**
     * 赛事启动状态
     */
    private String competitionStatus;

    /** 赛事收藏数 */
    private Integer competitionCollectNum;

    /** 赛事分享数 */
    private Integer competitionShareNum;

    /** 扩展字段 **/
    private String competitionExtension;

    /** 发布人 **/
    private Long publishPerson;

    /** 发布人名称 **/
    private String publishPersonName;

    /** 发布时间 **/
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date publishTime;

    /**
     * 版本
     */
    private Long version;

    /**
     * 删除标志
     */
    private String delFlag = "0";

    /** 数据权限用户id */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /**
     * 赛事界数列表
     */
    private List<CompetitionSeriesInfo> competitionSeriesInfoList;

    /**
     * 赛事阶段列表
     */
    private List<CompetitionStageConfig> competitionStageList;

//    /**
//     * 赛事奖品列表
//     */
//    private List<CompetitionAwardsConfig> competitionAwardsList;

    /**
     * 赛事课程列表
     */
    private List<CompetitionCourseConfig> competitionCourseConfigList;

    /**
     * 赛事企业关系列表
     */
    private List<CompetitionEnterpriseRela> competitionEnterpriseRelaList;

    /**
     * 赛事赛道列表
     */
    private List<CompetitionTrackInfo> competitionTrackList;

    /**
     * 赛事权限操作列表
     */
    private List<OperationConfig> operationConfigList;

    public Long getCompetitionId() {
        return competitionId;
    }

    public void setCompetitionId(Long competitionId) {
        this.competitionId = competitionId;
    }

    public String getCompetitionCode() {
        return competitionCode;
    }

    public void setCompetitionCode(String competitionCode) {
        this.competitionCode = competitionCode;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String getCompetitionName() {
        return competitionName;
    }

    public void setCompetitionName(String competitionName) {
        this.competitionName = competitionName;
    }

    public Long getCompetitionSeriesId() {
        return competitionSeriesId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public String getCompetitionSeriesName() {
        return competitionSeriesName;
    }

    public void setCompetitionSeriesName(String competitionSeriesName) {
        this.competitionSeriesName = competitionSeriesName;
    }

    public String getCompetitionType() {
        return competitionType;
    }

    public void setCompetitionType(String competitionType) {
        this.competitionType = competitionType;
    }

    public String getCompetitionDesc() {
        return competitionDesc;
    }

    public void setCompetitionDesc(String competitionDesc) {
        this.competitionDesc = competitionDesc;
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

    public String getCompetitionStatus() {
        return competitionStatus;
    }

    public void setCompetitionStatus(String competitionStatus) {
        this.competitionStatus = competitionStatus;
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

    public List<CompetitionStageConfig> getCompetitionStageList() {
        return competitionStageList;
    }

    public void setCompetitionStageList(List<CompetitionStageConfig> competitionStageList) {
        this.competitionStageList = competitionStageList;
    }

    public List<CompetitionCourseConfig> getCompetitionCourseConfigList() {
        return competitionCourseConfigList;
    }

    public void setCompetitionCourseConfigList(List<CompetitionCourseConfig> competitionCourseConfigList) {
        this.competitionCourseConfigList = competitionCourseConfigList;
    }

    public List<CompetitionEnterpriseRela> getCompetitionEnterpriseRelaList() {
        return competitionEnterpriseRelaList;
    }

    public void setCompetitionEnterpriseRelaList(List<CompetitionEnterpriseRela> competitionEnterpriseRelaList) {
        this.competitionEnterpriseRelaList = competitionEnterpriseRelaList;
    }

    public String getCompetitionNumber() {
        return competitionNumber;
    }

    public void setCompetitionNumber(String competitionNumber) {
        this.competitionNumber = competitionNumber;
    }

    public String getCompetitionImage() {
        return competitionImage;
    }

    public void setCompetitionImage(String competitionImage) {
        this.competitionImage = competitionImage;
    }

    public String getEnterpriseId() {
        return enterpriseId;
    }

    public void setEnterpriseId(String enterpriseId) {
        this.enterpriseId = enterpriseId;
    }

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public Long getOrgId() {
        return orgId;
    }

    public void setOrgId(Long orgId) {
        this.orgId = orgId;
    }

    public String getCompetitionImageName() {
        return competitionImageName;
    }

    public void setCompetitionImageName(String competitionImageName) {
        this.competitionImageName = competitionImageName;
    }

    public String getCompetitionTypeCn() {
        return competitionTypeCn;
    }

    public void setCompetitionTypeCn(String competitionTypeCn) {
        this.competitionTypeCn = competitionTypeCn;
    }

    public Integer getCompetitionCollectNum() {
        return competitionCollectNum;
    }

    public void setCompetitionCollectNum(Integer competitionCollectNum) {
        this.competitionCollectNum = competitionCollectNum;
    }

    public Integer getCompetitionShareNum() {
        return competitionShareNum;
    }

    public void setCompetitionShareNum(Integer competitionShareNum) {
        this.competitionShareNum = competitionShareNum;
    }

    public String getCompetitionExtension() {
        return competitionExtension;
    }

    public void setCompetitionExtension(String competitionExtension) {
        this.competitionExtension = competitionExtension;
    }

    public Long getPublishPerson() {
        return publishPerson;
    }

    public void setPublishPerson(Long publishPerson) {
        this.publishPerson = publishPerson;
    }

    public String getPublishPersonName() {
        return publishPersonName;
    }

    public void setPublishPersonName(String publishPersonName) {
        this.publishPersonName = publishPersonName;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public List<CompetitionTrackInfo> getCompetitionTrackList() {
        return competitionTrackList;
    }

    public void setCompetitionTrackList(List<CompetitionTrackInfo> competitionTrackList) {
        this.competitionTrackList = competitionTrackList;
    }

    public List<CompetitionSeriesInfo> getCompetitionSeriesInfoList() {
        return competitionSeriesInfoList;
    }

    public void setCompetitionSeriesInfoList(List<CompetitionSeriesInfo> competitionSeriesInfoList) {
        this.competitionSeriesInfoList = competitionSeriesInfoList;
    }

    public List<OperationConfig> getOperationConfigList() {
        return operationConfigList;
    }

    public void setOperationConfigList(List<OperationConfig> operationConfigList) {
        this.operationConfigList = operationConfigList;
    }
}
