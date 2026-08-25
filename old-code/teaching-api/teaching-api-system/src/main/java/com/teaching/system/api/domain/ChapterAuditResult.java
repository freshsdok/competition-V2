package com.teaching.system.api.domain;

import java.util.List;

/**
 * 章节视频审核结果
 *
 * @author Administrator
 */
public class ChapterAuditResult {
    /**
     * 章节id
     */
    private Long chapterId;

    /**
     * 章节下各个视频的审核信息
     */
    private List<PageInfo> pageInfo;

    public Long getChapterId() {
        return chapterId;
    }

    public void setChapterId(Long chapterId) {
        this.chapterId = chapterId;
    }

    public List<PageInfo> getPageInfo() {
        return pageInfo;
    }

    public void setPageInfo(List<PageInfo> pageInfo) {
        this.pageInfo = pageInfo;
    }
}
