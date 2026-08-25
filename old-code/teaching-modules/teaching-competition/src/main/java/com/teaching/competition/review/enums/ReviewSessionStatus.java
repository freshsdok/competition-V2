package com.teaching.competition.review.enums;

    /**
     * ReviewSessionStatus枚举。
     */
    public enum ReviewSessionStatus {
NOT_STARTED("NOT_STARTED", "未开始"),
IN_PROGRESS("IN_PROGRESS", "进行中"),
PAUSED("PAUSED", "已暂停"),
ENDED("ENDED", "已结束"),
ARCHIVED("ARCHIVED", "已归档");

        private final String code;
        private final String desc;

        ReviewSessionStatus(String code, String desc) {
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
