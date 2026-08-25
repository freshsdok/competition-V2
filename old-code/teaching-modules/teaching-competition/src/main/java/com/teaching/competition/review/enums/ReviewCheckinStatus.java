package com.teaching.competition.review.enums;

    /**
     * ReviewCheckinStatus枚举。
     */
    public enum ReviewCheckinStatus {
WAITING("WAITING", "待签到"),
PRESENT("PRESENT", "已到场"),
ABSENT("ABSENT", "缺席"),
LATE("LATE", "迟到");

        private final String code;
        private final String desc;

        ReviewCheckinStatus(String code, String desc) {
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
