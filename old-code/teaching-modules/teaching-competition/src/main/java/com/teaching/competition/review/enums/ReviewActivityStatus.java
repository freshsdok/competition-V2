package com.teaching.competition.review.enums;

    /**
     * ReviewActivityStatus枚举。
     */
    public enum ReviewActivityStatus {
DRAFT("DRAFT", "草稿"),
SUBMITTING("SUBMITTING", "填报中"),
SUBMIT_CLOSED("SUBMIT_CLOSED", "填报截止"),
REVIEWING("REVIEWING", "评审中"),
SUMMARYING("SUMMARYING", "汇总中"),
PUBLISHED("PUBLISHED", "已发布"),
ARCHIVED("ARCHIVED", "已归档"),
DISABLED("DISABLED", "已停用");

        private final String code;
        private final String desc;

        ReviewActivityStatus(String code, String desc) {
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
