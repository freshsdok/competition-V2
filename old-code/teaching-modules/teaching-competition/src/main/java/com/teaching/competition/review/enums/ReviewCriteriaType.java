package com.teaching.competition.review.enums;

    /**
     * ReviewCriteriaType枚举。
     */
    public enum ReviewCriteriaType {
NUMBER("NUMBER", "数字评分"),
SINGLE_CHOICE("SINGLE_CHOICE", "单选项"),
TEXT("TEXT", "文本评价");

        private final String code;
        private final String desc;

        ReviewCriteriaType(String code, String desc) {
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
