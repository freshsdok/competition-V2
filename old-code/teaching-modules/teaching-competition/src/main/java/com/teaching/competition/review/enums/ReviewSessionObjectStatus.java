package com.teaching.competition.review.enums;

    /**
     * ReviewSessionObjectStatus枚举。
     */
    public enum ReviewSessionObjectStatus {
WAITING("WAITING", "等待中"),
REVIEWING("REVIEWING", "评审中"),
SCORED("SCORED", "已评分"),
COMPLETED("COMPLETED", "已完成"),
SKIPPED("SKIPPED", "已跳过"),
DELAYED("DELAYED", "已延后");

        private final String code;
        private final String desc;

        ReviewSessionObjectStatus(String code, String desc) {
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
