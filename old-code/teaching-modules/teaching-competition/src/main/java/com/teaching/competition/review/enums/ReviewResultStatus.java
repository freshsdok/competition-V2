package com.teaching.competition.review.enums;

    /**
     * ReviewResultStatus枚举。
     */
    public enum ReviewResultStatus {
GENERATED("GENERATED", "已生成"),
PUBLISHED("PUBLISHED", "已发布"),
REVOKED("REVOKED", "已撤销"),
ARCHIVED("ARCHIVED", "已归档");

        private final String code;
        private final String desc;

        ReviewResultStatus(String code, String desc) {
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
