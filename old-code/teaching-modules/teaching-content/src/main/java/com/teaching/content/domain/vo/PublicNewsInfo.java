package com.teaching.content.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;

import java.io.Serializable;
import java.util.Date;

/**
 * 面向匿名访问者的资讯详情。
 *
 * <p>该对象只包含前台展示所需字段，避免将后台审计、权限和流程字段暴露给公开接口。</p>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PublicNewsInfo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long newsId;

    private String newsTitle;

    private String newsViceTitle;

    private String newsCont;

    private String newsAbstract;

    private String newsImage;

    private String newsTag;

    private String newsAuthor;

    private String newsSource;

    private String newsType;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date publishTime;

    private Long classifyId;

    private Integer readingQuantity;

    private Integer likesNum;

    private String isTop;

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
}
