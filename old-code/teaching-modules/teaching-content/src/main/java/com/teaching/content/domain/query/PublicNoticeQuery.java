package com.teaching.content.domain.query;

/**
 * 匿名通知公告列表允许使用的查询条件。
 *
 * <p>有意不继承 BaseEntity，避免匿名请求绑定 params、审计字段或数据权限 SQL 片段。</p>
 */
public class PublicNoticeQuery {

    private String noticeTitle;

    private String noticeType;

    private String noticeAuthor;

    public String getNoticeTitle() {
        return noticeTitle;
    }

    public void setNoticeTitle(String noticeTitle) {
        this.noticeTitle = noticeTitle;
    }

    public String getNoticeType() {
        return noticeType;
    }

    public void setNoticeType(String noticeType) {
        this.noticeType = noticeType;
    }

    public String getNoticeAuthor() {
        return noticeAuthor;
    }

    public void setNoticeAuthor(String noticeAuthor) {
        this.noticeAuthor = noticeAuthor;
    }
}
