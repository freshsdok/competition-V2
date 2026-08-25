package com.teaching.competition.review.enums;

    /**
     * ReviewMemberRole枚举。
     */
    public enum ReviewMemberRole {
LEADER("LEADER", "负责人"),
MEMBER("MEMBER", "参加人"),
CONTACT("CONTACT", "联系人"),
TEACHER("TEACHER", "指导教师"),
OTHER("OTHER", "其他");

        private final String code;
        private final String desc;

        ReviewMemberRole(String code, String desc) {
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
