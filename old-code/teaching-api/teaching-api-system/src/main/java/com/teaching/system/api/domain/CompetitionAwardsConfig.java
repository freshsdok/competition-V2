package com.teaching.system.api.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 赛事奖项设置对象 competition_awards_config
 * 
 * @author teaching
 * @date 2025-10-11
 */
public class CompetitionAwardsConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 奖项id */
    private String awardsId;

    /** 赛事系列id */
    @Excel(name = "赛事系列id")
    private Long competitionSeriesId;

    /** 赛事阶段id */
    private String stageId;

    /** 奖项名称 */
    @Excel(name = "奖项名称")
    private String awardsName;

    /** 获奖人数/团队数 */
    @Excel(name = "获奖人数/团队数")
    private String awardNum;

    /** 奖金金额 */
    @Excel(name = "奖金金额")
    private String bonusNum;

    /** 奖品描述 */
    @Excel(name = "奖品描述")
    private String awardDesc;

    /** 赛道名称 */
    private String competitionTrackName;

    /** 组别 */
    private String groupClassify;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 删除标识 */
    private String delFlag = "0";

    /** 数据权限用户id */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    /** 已分配数量 */
    private String allocatedNum;

    /** 未分配数量 */
    private String unabsorbedNum;

    public String getAwardsId() {
        return awardsId;
    }

    public void setAwardsId(String awardsId) {
        this.awardsId = awardsId;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId() 
    {
        return competitionSeriesId;
    }

    public void setAwardsName(String awardsName) 
    {
        this.awardsName = awardsName;
    }

    public String getAwardsName() 
    {
        return awardsName;
    }

    public void setAwardNum(String awardNum) 
    {
        this.awardNum = awardNum;
    }

    public String getAwardNum() 
    {
        return awardNum;
    }

    public void setBonusNum(String bonusNum) 
    {
        this.bonusNum = bonusNum;
    }

    public String getBonusNum() 
    {
        return bonusNum;
    }

    public void setAwardDesc(String awardDesc) 
    {
        this.awardDesc = awardDesc;
    }

    public String getAwardDesc() 
    {
        return awardDesc;
    }

    public void setVersion(Long version) 
    {
        this.version = version;
    }

    public Long getVersion() 
    {
        return version;
    }

    public void setDelFlag(String delFlag) 
    {
        this.delFlag = delFlag;
    }

    public String getDelFlag() 
    {
        return delFlag;
    }

    public void setUserId(Long userId) 
    {
        this.userId = userId;
    }

    public Long getUserId() 
    {
        return userId;
    }

    public void setOrgId(Long orgId) 
    {
        this.orgId = orgId;
    }

    public Long getOrgId() 
    {
        return orgId;
    }

    public String getStageId() {
        return stageId;
    }

    public void setStageId(String stageId) {
        this.stageId = stageId;
    }

    public String getAllocatedNum() {
        return allocatedNum;
    }

    public void setAllocatedNum(String allocatedNum) {
        this.allocatedNum = allocatedNum;
    }

    public String getUnabsorbedNum() {
        return unabsorbedNum;
    }

    public void setUnabsorbedNum(String unabsorbedNum) {
        this.unabsorbedNum = unabsorbedNum;
    }

    public String getCompetitionTrackName() {
        return competitionTrackName;
    }

    public void setCompetitionTrackName(String competitionTrackName) {
        this.competitionTrackName = competitionTrackName;
    }

    public String getGroupClassify() {
        return groupClassify;
    }

    public void setGroupClassify(String groupClassify) {
        this.groupClassify = groupClassify;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("awardsId", getAwardsId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("stageId", getStageId())
            .append("awardsName", getAwardsName())
            .append("awardNum", getAwardNum())
            .append("bonusNum", getBonusNum())
            .append("awardDesc", getAwardDesc())
            .append("competitionTrackName", getCompetitionTrackName())
            .append("groupClassify", getGroupClassify())
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
