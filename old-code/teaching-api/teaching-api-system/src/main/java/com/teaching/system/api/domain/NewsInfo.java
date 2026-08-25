package com.teaching.system.api.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

/**
 * 资讯信息对象 news_info (API模块)
 *
 * @author teaching
 * @date 2025-10-27
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NewsInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * 资讯ID
     */
    private Long newsId;

    /**
     * 资讯标题
     */
    private String newsTitle;

    /**
     * 资讯副标题
     */
    private String newsViceTitle;

    /**
     * 资讯内容
     */
    private String newsCont;

    /**
     * 资讯摘要
     */
    private String newsAbstract;

    /**
     * 资讯图片
     */
    private String newsImage;

    /**
     * 资讯标签
     */
    private String newsTag;

    /**
     * 资讯作者
     */
    private String newsAuthor;

    /**
     * 资讯来源
     */
    private String newsSource;

    /**
     * 资讯类型（字典：news_type，赛事/课程）
     */
    private String newsType;

    /**
     * 发布时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    /**
     * 状态：草稿/审核中/审核通过/审核驳回/已发布/已下架
     */
    private String newsStatus;

    /**
     * 分类ID
     */
    private Long classifyId;

    /**
     * 阅读量
     */
    private Integer readingQuantity;

    /**
     * 点赞数
     */
    private Integer likesNum;

    /**
     * 是否置顶（0否 1是）
     */
    private String isTop;

    /**
     * 审核状态
     */
    private String checkStatus;

    /**
     * 创建者
     */
    private String createBy;

    /**
     * 创建时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date createTime;

    /**
     * 更新者
     */
    private String updateBy;

    /**
     * 更新时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date updateTime;

    /**
     * 备注
     */
    private String remark;

    /**
     * 无参构造函数
     */
    public NewsInfo() {
    }

    /**
     * 便捷构造函数（用于跨服务调用修改审核状态）
     *
     * @param newsId      资讯ID
     * @param checkStatus 审核状态
     */
    public NewsInfo(Long newsId, String checkStatus) {
        this.newsId = newsId;
        this.checkStatus = checkStatus;
    }

    public Long getNewsId() {
        return newsId;
    }

    public void setNewsId(Long newsId) {
        this.newsId = newsId;
    }

    public String getNewsTitle() {
        return newsTitle;
    }

    public void setNewsTitle(String newsTitle) {
        this.newsTitle = newsTitle;
    }

    public String getNewsViceTitle() {
        return newsViceTitle;
    }

    public void setNewsViceTitle(String newsViceTitle) {
        this.newsViceTitle = newsViceTitle;
    }

    public String getNewsCont() {
        return newsCont;
    }

    public void setNewsCont(String newsCont) {
        this.newsCont = newsCont;
    }

    public String getNewsAbstract() {
        return newsAbstract;
    }

    public void setNewsAbstract(String newsAbstract) {
        this.newsAbstract = newsAbstract;
    }

    public String getNewsImage() {
        return newsImage;
    }

    public void setNewsImage(String newsImage) {
        this.newsImage = newsImage;
    }

    public String getNewsTag() {
        return newsTag;
    }

    public void setNewsTag(String newsTag) {
        this.newsTag = newsTag;
    }

    public String getNewsAuthor() {
        return newsAuthor;
    }

    public void setNewsAuthor(String newsAuthor) {
        this.newsAuthor = newsAuthor;
    }

    public String getNewsSource() {
        return newsSource;
    }

    public void setNewsSource(String newsSource) {
        this.newsSource = newsSource;
    }

    public String getNewsType() {
        return newsType;
    }

    public void setNewsType(String newsType) {
        this.newsType = newsType;
    }

    public Date getPublishTime() {
        return publishTime;
    }

    public void setPublishTime(Date publishTime) {
        this.publishTime = publishTime;
    }

    public String getNewsStatus() {
        return newsStatus;
    }

    public void setNewsStatus(String newsStatus) {
        this.newsStatus = newsStatus;
    }

    public Long getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }

    public Integer getReadingQuantity() {
        return readingQuantity;
    }

    public void setReadingQuantity(Integer readingQuantity) {
        this.readingQuantity = readingQuantity;
    }

    public Integer getLikesNum() {
        return likesNum;
    }

    public void setLikesNum(Integer likesNum) {
        this.likesNum = likesNum;
    }

    public String getIsTop() {
        return isTop;
    }

    public void setIsTop(String isTop) {
        this.isTop = isTop;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }

    public String getCreateBy() {
        return createBy;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public String getRemark() {
        return remark;
    }

    public void setRemark(String remark) {
        this.remark = remark;
    }
}
