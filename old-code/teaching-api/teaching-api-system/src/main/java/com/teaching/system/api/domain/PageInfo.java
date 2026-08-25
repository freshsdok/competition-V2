package com.teaching.system.api.domain;

/**
 * pageInfo 原用于跨服务修改页面的状态
 * 可以扩展用于此类型的 通过主键修（Long型）改某个值（字符串）
 *
 * @author Administrator
 */
public class PageInfo {
    /**
     * id
     */
    private Long pageId;

    /**
     * 审核状态 整个流程审核状态（字典check_status）  2待审核，3审核中，4已通过，5已拒绝
     */
    private String checkStatus;
    /**
     * 审核意见
     */
    private String applyReason;

    public PageInfo() {
    }

    public PageInfo(Long pageId, String checkStatus) {
        this.pageId = pageId;
        this.checkStatus = checkStatus;
    }

    public PageInfo(Long pageId, String checkStatus, String applyReason) {
        this.pageId = pageId;
        this.checkStatus = checkStatus;
        this.applyReason = applyReason;
    }

    public String getApplyReason() {
        return applyReason;
    }

    public void setApplyReason(String applyReason) {
        this.applyReason = applyReason;
    }

    public Long getPageId() {
        return pageId;
    }

    public void setPageId(Long pageId) {
        this.pageId = pageId;
    }

    public String getCheckStatus() {
        return checkStatus;
    }

    public void setCheckStatus(String checkStatus) {
        this.checkStatus = checkStatus;
    }
}
