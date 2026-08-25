package com.teaching.competition.review.enums;

    /**
     * ReviewSubmissionMode枚举。
     */
    public enum ReviewSubmissionMode {
OPEN("OPEN", "开放填报"),
ASSIGNED_USER("ASSIGNED_USER", "指定用户"),
BUSINESS_IMPORTED("BUSINESS_IMPORTED", "业务导入");

        private final String code;
        private final String desc;

        ReviewSubmissionMode(String code, String desc) {
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
