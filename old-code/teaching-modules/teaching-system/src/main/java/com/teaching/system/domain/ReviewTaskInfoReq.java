package com.teaching.system.domain;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.teaching.common.core.annotation.Excel;
import com.teaching.common.core.web.domain.BaseEntity;

import java.util.Date;
import java.util.List;

public class ReviewTaskInfoReq extends BaseEntity {
    private static final long serialVersionUID = 1L;

    private List<Long> reviewIdList;

    /**
     * 审阅开始时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewStartTime;

    /**
     * 审阅结束时间
     */
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private Date reviewEndTime;

    /**
     * 评审备注
     */
    private String reviewDesc;

    /**
     * 所在组ID集合
     */
    private String reviewGroupId;

    /**
     * 参考文档
     */
    private String referenceDocument;

    public List<Long> getReviewIdList() {
        return reviewIdList;
    }

    public void setReviewIdList(List<Long> reviewIdList) {
        this.reviewIdList = reviewIdList;
    }

    public Date getReviewStartTime() {
        return reviewStartTime;
    }

    public void setReviewStartTime(Date reviewStartTime) {
        this.reviewStartTime = reviewStartTime;
    }

    public Date getReviewEndTime() {
        return reviewEndTime;
    }

    public void setReviewEndTime(Date reviewEndTime) {
        this.reviewEndTime = reviewEndTime;
    }

    public String getReviewDesc() {
        return reviewDesc;
    }

    public void setReviewDesc(String reviewDesc) {
        this.reviewDesc = reviewDesc;
    }

    public String getReviewGroupId() {
        return reviewGroupId;
    }

    public void setReviewGroupId(String reviewGroupId) {
        this.reviewGroupId = reviewGroupId;
    }

    public String getReferenceDocument() {
        return referenceDocument;
    }

    public void setReferenceDocument(String referenceDocument) {
        this.referenceDocument = referenceDocument;
    }
}
