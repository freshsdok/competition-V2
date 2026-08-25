package com.teaching.competition.review.enums;

    /**
     * ReviewRoundStatus枚举。
     */
    public enum ReviewRoundStatus {
DRAFT("DRAFT", "草稿"),
NOT_STARTED("NOT_STARTED", "未开始"),
IN_PROGRESS("IN_PROGRESS", "进行中"),
ENDED("ENDED", "已结束"),
ARCHIVED("ARCHIVED", "已归档"),
DISABLED("DISABLED", "已停用");

        private final String code;
        private final String desc;

        ReviewRoundStatus(String code, String desc) {
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
