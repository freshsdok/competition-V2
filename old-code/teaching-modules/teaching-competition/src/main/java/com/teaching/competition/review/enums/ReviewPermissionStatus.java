package com.teaching.competition.review.enums;

/**
 * 填报权限状态枚举。
 */
public enum ReviewPermissionStatus {
    ACTIVE("ACTIVE", "有效"),
    USED("USED", "已使用"),
    EXPIRED("EXPIRED", "已过期"),
    DISABLED("DISABLED", "已停用");

    private final String code;
    private final String desc;

    ReviewPermissionStatus(String code, String desc) {
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
