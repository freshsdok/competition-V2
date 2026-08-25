package com.teaching.competition.review.enums;

    /**
     * ReviewAssignmentStatus枚举。
     */
    public enum ReviewAssignmentStatus {
ASSIGNED("ASSIGNED", "已分配"),
IN_PROGRESS("IN_PROGRESS", "评审中"),
SUBMITTED("SUBMITTED", "已提交"),
RETURNED("RETURNED", "已退回"),
LOCKED("LOCKED", "已锁定"),
CANCELLED("CANCELLED", "已取消");

        private final String code;
        private final String desc;

        ReviewAssignmentStatus(String code, String desc) {
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
