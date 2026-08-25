package com.teaching.competition.review.enums;

/**
 * 评分完成度状态。
 */
public enum ReviewCompletionStatus {
    NOT_STARTED("NOT_STARTED", "未开始"),
    PARTIAL("PARTIAL", "部分完成"),
    COMPLETED("COMPLETED", "已完成");

    private final String code;
    private final String desc;

    ReviewCompletionStatus(String code, String desc) {
        this.code = code;
        this.desc = desc;
    }

    public String getCode() {
        return code;
    }

    public String getDesc() {
        return desc;
    }
}
