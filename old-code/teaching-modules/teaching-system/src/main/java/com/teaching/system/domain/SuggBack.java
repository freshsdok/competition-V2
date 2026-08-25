package com.teaching.system.domain;

import java.util.Date;
import java.util.List;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.annotation.Excel.ColumnType;
import com.teaching.common.core.web.domain.BaseEntity;
import com.teaching.common.core.xss.Xss;

/**
 * 意见反馈表 suggestion_feedback_info
 *
 * @author teaching
 */
public class SuggBack extends BaseEntity
{
    private static final long serialVersionUID = 1L;

    /** 反馈ID */
//    @Excel(name = "反馈ID", cellType = ColumnType.NUMERIC)
    private Long suggBackId;

    /** 反馈编码 */
    @Excel(name = "反馈编号")
    private String backCode;

    /** 反馈类型 */
    @Excel(name = "反馈类型", readConverterExp = "功能建议=功能建议,问题反馈=问题反馈,投诉举报=投诉举报,其他=其他")
    private String type;

    /** 反馈标题 */
    @Excel(name = "反馈标题")
    private String title;

    /** 用户姓名 */
    @Excel(name = "用户名称")
    private String userName;

    /** 反馈内容 */
    @Excel(name = "反馈内容")
    private String content;

    /** 联系电话 */
    @Excel(name = "联系电话")
    private String phone;

    /** 处理状态（0待处理 1处理中 2已回复 3已关闭） */
    @Excel(name = "处理状态", readConverterExp = "0=待处理,1=处理中,2=已回复,3=已关闭")
    private String dealStatus;

    /** 反馈时间 */
    @Excel(name = "反馈时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date suggTime;

    /** 处理结果 */
//    @Excel(name = "处理结果")
    private String dealResult;

    /** 回复内容 */
    @Excel(name = "回复内容")
    private String replyContent;

    /** 回复时间 */
    @Excel(name = "回复时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date replyTime;

    /** 满意度评价 */
//    @Excel(name = "满意度评价")
    private String saftEval;

    /** 用户ID */
    private Long userId;

    /** 组织ID */
    private Long orgId;

    /** 版本号 */
    private Integer version;

    /** 删除标志 */
    private String delFlag;

    /** 文件列表 */
    private List<SuggBackFile> files;

    public Long getSuggBackId()
    {
        return suggBackId;
    }

    public void setSuggBackId(Long suggBackId)
    {
        this.suggBackId = suggBackId;
    }

    @Size(min = 0, max = 50, message = "反馈编码长度不能超过50个字符")
    public String getBackCode()
    {
        return backCode;
    }

    public void setBackCode(String backCode)
    {
        this.backCode = backCode;
    }

    @NotBlank(message = "反馈类型不能为空")
    public String getType()
    {
        return type;
    }

    public void setType(String type)
    {
        this.type = type;
    }

    @Xss(message = "反馈标题不能包含脚本字符")
    @Size(min = 0, max = 200, message = "反馈标题不能超过200个字符")
    public String getTitle()
    {
        return title;
    }

    public void setTitle(String title)
    {
        this.title = title;
    }

    @Size(min = 0, max = 50, message = "用户姓名长度不能超过50个字符")
    public String getUserName()
    {
        return userName;
    }

    public void setUserName(String userName)
    {
        this.userName = userName;
    }

    @Xss(message = "反馈内容不能包含脚本字符")
    @NotBlank(message = "反馈内容不能为空")
    @Size(min = 0, max = 2000, message = "反馈内容不能超过2000个字符")
    public String getContent()
    {
        return content;
    }

    public void setContent(String content)
    {
        this.content = content;
    }

    @Size(min = 0, max = 20, message = "联系电话长度不能超过20个字符")
    public String getPhone()
    {
        return phone;
    }

    public void setPhone(String phone)
    {
        this.phone = phone;
    }

    public String getDealStatus()
    {
        return dealStatus;
    }

    public void setDealStatus(String dealStatus)
    {
        this.dealStatus = dealStatus;
    }

    public Date getSuggTime()
    {
        return suggTime;
    }

    public void setSuggTime(Date suggTime)
    {
        this.suggTime = suggTime;
    }

    @Size(min = 0, max = 1000, message = "处理结果长度不能超过1000个字符")
    public String getDealResult()
    {
        return dealResult;
    }

    public void setDealResult(String dealResult)
    {
        this.dealResult = dealResult;
    }

    @Size(min = 0, max = 1000, message = "回复内容长度不能超过1000个字符")
    public String getReplyContent()
    {
        return replyContent;
    }

    public void setReplyContent(String replyContent)
    {
        this.replyContent = replyContent;
    }

    public Date getReplyTime()
    {
        return replyTime;
    }

    public void setReplyTime(Date replyTime)
    {
        this.replyTime = replyTime;
    }

    public String getSaftEval()
    {
        return saftEval;
    }

    public void setSaftEval(String saftEval)
    {
        this.saftEval = saftEval;
    }

    public Long getUserId()
    {
        return userId;
    }

    public void setUserId(Long userId)
    {
        this.userId = userId;
    }

    public Long getOrgId()
    {
        return orgId;
    }

    public void setOrgId(Long orgId)
    {
        this.orgId = orgId;
    }

    public Integer getVersion()
    {
        return version;
    }

    public void setVersion(Integer version)
    {
        this.version = version;
    }

    public String getDelFlag()
    {
        return delFlag;
    }

    public void setDelFlag(String delFlag)
    {
        this.delFlag = delFlag;
    }

    public List<SuggBackFile> getFiles()
    {
        return files;
    }

    public void setFiles(List<SuggBackFile> files)
    {
        this.files = files;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this,ToStringStyle.MULTI_LINE_STYLE)
            .append("suggBackId", getSuggBackId())
            .append("backCode", getBackCode())
            .append("type", getType())
            .append("title", getTitle())
            .append("userName", getUserName())
            .append("content", getContent())
            .append("phone", getPhone())
            .append("dealStatus", getDealStatus())
            .append("suggTime", getSuggTime())
            .append("dealResult", getDealResult())
            .append("replyContent", getReplyContent())
            .append("replyTime", getReplyTime())
            .append("saftEval", getSaftEval())
            .append("userId", getUserId())
            .append("orgId", getOrgId())
            .append("version", getVersion())
            .append("delFlag", getDelFlag())
            .append("files", getFiles())
            .append("createBy", getCreateBy())
            .append("createTime", getCreateTime())
            .append("updateBy", getUpdateBy())
            .append("updateTime", getUpdateTime())
            .toString();
    }
}
