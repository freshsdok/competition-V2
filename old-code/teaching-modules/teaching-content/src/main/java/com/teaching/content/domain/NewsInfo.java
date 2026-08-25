package com.teaching.content.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;
import jakarta.validation.constraints.NotBlank;
import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;

import java.util.Date;

/**
 * 资讯信息对象 news_info
 *
 * @author teaching
 * @date 2025-10-27
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsInfo extends BaseEntity {
    private static final long serialVersionUID = 1L;

    /**
     * 资讯ID
     */
    private Long newsId;

    /**
     * 资讯标题
     */
    @Excel(name = "资讯标题")
    @NotBlank(message = "资讯标题不能为空")
    private String newsTitle;

    /**
     * 资讯副标题
     */
    @Excel(name = "资讯副标题")
    private String newsViceTitle;

    /**
     * 资讯内容
     */
    @Excel(name = "资讯内容")
    private String newsCont;

    /**
     * 资讯摘要
     */
    @Excel(name = "资讯摘要")
    @NotBlank(message = "资讯摘要不能为空")
    private String newsAbstract;

    /**
     * 资讯图片
     */
    @Excel(name = "资讯图片")
    private String newsImage;

    /**
     * 资讯标签
     */
    @Excel(name = "资讯标签")
    private String newsTag;

    /**
     * 资讯作者
     */
    @Excel(name = "资讯作者")
    private String newsAuthor;

    /**
     * 资讯来源
     */
    @Excel(name = "资讯来源")
    private String newsSource;

    /**
     * 资讯类型（字典：news_type，赛事/课程）
     */
    @Excel(name = "资讯类型", readConverterExp = "1=赛事,2=课程")
    private String newsType;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Excel(name = "发布时间", width = 30, dateFormat = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /**
     * 状态：草稿/审核中/审核通过/审核驳回/已发布/已下架
     */
    @Excel(name = "状态", readConverterExp = "草稿=草稿,审核中=审核中,审核通过=审核通过,审核驳回=审核驳回,已发布=已发布,已下架=已下架")
    private String newsStatus;

    /**
     * 分类ID
     */
    @Excel(name = "分类ID")
    private Long classifyId;

    /**
     * 阅读量
     */
    @Excel(name = "阅读量")
    private Integer readingQuantity;

    /**
     * 点赞数
     */
    @Excel(name = "点赞数")
    private Integer likesNum;

    /**
     * 是否置顶（0否 1是）
     */
    @Excel(name = "是否置顶", readConverterExp = "0=否,1=是")
    private String isTop;

    /**
     * 审核状态
     */
    @Excel(name = "审核状态")
    private String checkStatus;

    /**
     * 版本号
     */
    private Integer version;

    /**
     * 删除标志（0代表存在 2代表删除）
     */
    private String delFlag;

    /**
     * 数据权限用户id
     */
    private Long userId;

    /**
     * 数据权限机构id
     */
    private Long orgId;

    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }

    public Long getNewsId() {
        return newsId;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsViceTitle(String newsViceTitle) {
        this.newsViceTitle = newsViceTitle;
    }

    public String getNewsViceTitle() {
        return newsViceTitle;
    }

    public void setNewsCont(String newsCont) {
        this.newsCont = newsCont;
    }

    public String getNewsCont() {
        return newsCont;
    }

    public void setNewsAbstract(String newsAbstract) {
        this.newsAbstract = newsAbstract;
    }

    public String getNewsAbstract() {
        return newsAbstract;
    }

    public void setNewsImage(String newsImage) {
        this.newsImage = newsImage;
    }

    public String getNewsImage() {
        return newsImage;
    }

    public void setNewsTag(String newsTag) {
        this.newsTag = newsTag;
    }

    public String getNewsTag() {
        return newsTag;
    }

    public void setNewsAuthor(String newsAuthor) {
        this.newsAuthor = newsAuthor;
    }

    public String getNewsAuthor() {
        return newsAuthor;
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public String getNewsSource() {
        return newsSource;
    }

    public void setNewsType(String newsType) {
        this.newsType = newsType;
    }

    public String getNewsType() {
        return newsType;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setNewsStatus(String newsStatus) {
        this.newsStatus = newsStatus;
    }

    public String getNewsStatus() {
        return newsStatus;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }

    public Long getClassifyId() {
        return classifyId;
    }

    public void setReadingQuantity(Integer readingQuantity) {
        this.readingQuantity = readingQuantity;
    }

    public Integer getReadingQuantity() {
        return readingQuantity;
    }

    public void setLikesNum(Integer likesNum) {
        this.likesNum = likesNum;
    }

    public Integer getLikesNum() {
        return likesNum;
    }

    public void setIsTop(String isTop) {
        this.isTop = isTop;
    }

    public String getIsTop() {
        return isTop;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }

    public Integer getVersion() {
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
                .append("newsId", getNewsId())
                .append("newsTitle", getNewsTitle())
                .append("newsViceTitle", getNewsViceTitle())
                .append("newsCont", getNewsCont())
                .append("newsAbstract", getNewsAbstract())
                .append("newsImage", getNewsImage())
                .append("newsTag", getNewsTag())
                .append("newsAuthor", getNewsAuthor())
                .append("newsSource", getNewsSource())
                .append("newsType", getNewsType())
                .append("publishTime", getPublishTime())
                .append("newsStatus", getNewsStatus())
                .append("classifyId", getClassifyId())
                .append("readingQuantity", getReadingQuantity())
                .append("likesNum", getLikesNum())
                .append("isTop", getIsTop())
                .append("checkStatus", getCheckStatus())
                .append("createBy", getCreateBy())
                .append("createTime", getCreateTime())
                .append("updateBy", getUpdateBy())
                .append("updateTime", getUpdateTime())
                .append("version", getVersion())
                .append("delFlag", getDelFlag())
                .append("userId", getUserId())
                .append("orgId", getOrgId())
                .append("remark", getRemark())
                .toString();
    }
}
