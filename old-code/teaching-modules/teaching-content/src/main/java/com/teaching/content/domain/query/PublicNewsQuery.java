package com.teaching.content.domain.query;

/**
 * 匿名资讯列表允许使用的查询条件。
 *
 * <p>有意不继承 BaseEntity，避免匿名请求绑定 params、审计字段或数据权限 SQL 片段。</p>
 */
public class PublicNewsQuery {

    private String newsTitle;

    private String newsViceTitle;

    private String newsAbstract;

    private String newsAuthor;

    private String newsSource;

    private String newsType;

    private String newsTag;

    private Long classifyId;

    private String isTop;

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

    public String getNewsAbstract() {
        return newsAbstract;
    }

    public void setNewsAbstract(String newsAbstract) {
        this.newsAbstract = newsAbstract;
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

    public String getNewsTag() {
        return newsTag;
    }

    public void setNewsTag(String newsTag) {
        this.newsTag = newsTag;
    }

    public Long getClassifyId() {
        return classifyId;
    }

    public void setClassifyId(Long classifyId) {
        this.classifyId = classifyId;
    }

    public String getIsTop() {
        return isTop;
    }

    public void setIsTop(String isTop) {
        this.isTop = isTop;
    }
}
