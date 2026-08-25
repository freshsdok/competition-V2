package com.teaching.competition.review.enums;

    /**
     * ReviewRuleScoreMode枚举。
     */
    public enum ReviewRuleScoreMode {
SUM("SUM", "指标分数求和"),
WEIGHTED_SUM("WEIGHTED_SUM", "加权求和"),
AVERAGE("AVERAGE", "平均分");

        private final String code;
        private final String desc;

        ReviewRuleScoreMode(String code, String desc) {
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
