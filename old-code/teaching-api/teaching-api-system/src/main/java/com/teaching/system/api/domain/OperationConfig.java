package com.teaching.system.api.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

/**
 * 操作权限配置对象 operation_config
 *
 * @author teaching
 * @date 2026-01-24
 */
public class OperationConfig extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 主键 */
    private Long id;

    /** 大赛ID */
    @Excel(name = "大赛ID")
    private Long competitionSeriesId;

    /** 配置类型
     * 1:参赛信息修改 2:指导教师修改 3:退费重缴 4:退赛申请
     * */
    @Excel(name = "操作类型")
    private String operationType;

    /** 允许操作的用户类型["teacher","leader"] */
    @Excel(name = "允许操作的用户类型")
    private String allowedUserTypes;

    /** 允许时间区间[{"start":"2026-01-01 00:00:00","end":"2026-03-10 23:59:59"}] */
    @Excel(name = "允许时间区间")
    private String allowedTimeRanges;

    /**
     * 参赛信息修改的范围（1允许修改全部，2仅允许调整学生顺序）
     */
    private String modifyScope;

    /** 页面提示文字(富文本) */
    @Excel(name = "页面提示文字(富文本)")
    private String hintText1;

    /** 提交确认提示 */
    @Excel(name = "提交确认提示")
    private String hintText2;

    /** 附件模板路径["/template/1.docx"] */
    @Excel(name = "附件模板路径")
    private String attachments;

    /** 强制阅读时长(秒) */
    @Excel(name = "强制阅读时长(秒)")
    private Long forceReadSeconds;

    /** 次数限制：-1不限 */
    @Excel(name = "次数限制：-1不限")
    private Long maxTimes;

    /** 版本 */
    @Excel(name = "版本")
    private Long version;

    /** 0-存在，2-删除 */
    private String delFlag = "0";

    /** 数据权限用户id */
    @Excel(name = "数据权限用户id")
    private Long userId;

    /** 数据权限机构id */
    @Excel(name = "数据权限机构id")
    private Long orgId;

    public OperationConfig() {

    }
    public OperationConfig(Long competitionSeriesId) {
        this.competitionSeriesId = competitionSeriesId;
    }

    public OperationConfig(Long competitionSeriesId, String operationType) {
        this.competitionSeriesId = competitionSeriesId;
        this.operationType = operationType;
    }

    public void setId(Long id)
    {
        this.id = id;
    }

    public Long getId()
    {
        return id;
    }

    public void setCompetitionSeriesId(Long competitionSeriesId)
    {
        this.competitionSeriesId = competitionSeriesId;
    }

    public Long getCompetitionSeriesId()
    {
        return competitionSeriesId;
    }

    public void setOperationType(String operationType)
    {
        this.operationType = operationType;
    }

    public String getOperationType()
    {
        return operationType;
    }

    public void setAllowedUserTypes(String allowedUserTypes)
    {
        this.allowedUserTypes = allowedUserTypes;
    }

    public String getAllowedUserTypes()
    {
        return allowedUserTypes;
    }

    public void setAllowedTimeRanges(String allowedTimeRanges)
    {
        this.allowedTimeRanges = allowedTimeRanges;
    }

    public String getAllowedTimeRanges()
    {
        return allowedTimeRanges;
    }

    public void setHintText1(String hintText1)
    {
        this.hintText1 = hintText1;
    }

    public String getHintText1()
    {
        return hintText1;
    }

    public void setHintText2(String hintText2)
    {
        this.hintText2 = hintText2;
    }

    public String getHintText2()
    {
        return hintText2;
    }

    public void setAttachments(String attachments)
    {
        this.attachments = attachments;
    }

    public String getAttachments()
    {
        return attachments;
    }

    public void setForceReadSeconds(Long forceReadSeconds)
    {
        this.forceReadSeconds = forceReadSeconds;
    }

    public Long getForceReadSeconds()
    {
        return forceReadSeconds;
    }

    public void setMaxTimes(Long maxTimes)
    {
        this.maxTimes = maxTimes;
    }

    public Long getMaxTimes()
    {
        return maxTimes;
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

    public String getModifyScope() {
        return modifyScope;
    }

    public void setModifyScope(String modifyScope) {
        this.modifyScope = modifyScope;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("id", getId())
            .append("competitionSeriesId", getCompetitionSeriesId())
            .append("operationType", getOperationType())
            .append("allowedUserTypes", getAllowedUserTypes())
            .append("allowedTimeRanges", getAllowedTimeRanges())
            .append("hintText1", getHintText1())
            .append("hintText2", getHintText2())
            .append("attachments", getAttachments())
            .append("forceReadSeconds", getForceReadSeconds())
            .append("maxTimes", getMaxTimes())
            .append("createTime", getCreateTime())
            .append("updateTime", getUpdateTime())
            .append("createBy", getCreateBy())
            .append("updateBy", getUpdateBy())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .toString();
    }
}
