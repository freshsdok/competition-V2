package com.teaching.competition.review.enums;

    /**
     * ReviewObjectStatus枚举。
     */
    public enum ReviewObjectStatus {
DRAFT("DRAFT", "草稿"),
SUBMITTED("SUBMITTED", "已提交"),
WITHDRAW_REQUESTED("WITHDRAW_REQUESTED", "申请撤回"),
WITHDRAW_APPROVED("WITHDRAW_APPROVED", "撤回通过"),
WITHDRAW_REJECTED("WITHDRAW_REJECTED", "撤回驳回"),
LOCKED("LOCKED", "已锁定"),
INVALID("INVALID", "已作废"),
REVIEWING("REVIEWING", "评审中"),
REVIEWED("REVIEWED", "已评审"),
ARCHIVED("ARCHIVED", "已归档");

        private final String code;
        private final String desc;

        ReviewObjectStatus(String code, String desc) {
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
