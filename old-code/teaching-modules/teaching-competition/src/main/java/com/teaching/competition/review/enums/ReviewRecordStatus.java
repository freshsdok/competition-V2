package com.teaching.competition.review.enums;

    /**
     * ReviewRecordStatus枚举。
     */
    public enum ReviewRecordStatus {
DRAFT("DRAFT", "草稿"),
SUBMITTED("SUBMITTED", "已提交"),
RETURNED("RETURNED", "已退回"),
LOCKED("LOCKED", "已锁定"),
INVALID("INVALID", "已作废");

        private final String code;
        private final String desc;

        ReviewRecordStatus(String code, String desc) {
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
