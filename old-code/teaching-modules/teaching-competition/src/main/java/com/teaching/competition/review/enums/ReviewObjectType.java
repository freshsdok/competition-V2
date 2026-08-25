package com.teaching.competition.review.enums;

    /**
     * ReviewObjectType枚举。
     */
    public enum ReviewObjectType {
PROJECT("PROJECT", "项目"),
TEAM("TEAM", "团队"),
PERSON("PERSON", "个人"),
WORK("WORK", "作品"),
OTHER("OTHER", "其他");

        private final String code;
        private final String desc;

        ReviewObjectType(String code, String desc) {
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
