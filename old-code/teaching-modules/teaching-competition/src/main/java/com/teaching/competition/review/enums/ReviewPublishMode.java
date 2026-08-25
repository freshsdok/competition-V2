package com.teaching.competition.review.enums;

    /**
     * ReviewPublishMode枚举。
     */
    public enum ReviewPublishMode {
NONE("NONE", "不发布"),
MANUAL("MANUAL", "手动发布"),
AUTO("AUTO", "自动发布");

        private final String code;
        private final String desc;

        ReviewPublishMode(String code, String desc) {
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
